/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.client.internal.rest.jersey.subsystem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.Response;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.client.rest.jersey.ContextAwareUniformInterfaceException;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;

import com.sun.jersey.api.client.ClientResponse.Status;

import com.google.common.collect.Range;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.plexus.util.IOUtil;
import static com.google.common.base.Preconditions.checkState;

/**
 * @since 2.1
 */
public class JerseyContent
    extends SubsystemSupport<JerseyNexusClient>
    implements Content
{

  private static final String CONTENT_PREFIX = "content/";

  public JerseyContent(final JerseyNexusClient nexusClient) {
    super(nexusClient);
  }

  protected String toUri(final Location location, final Directive directive) {
    String uri = CONTENT_PREFIX + location.toContentPath();
    if (directive != null) {
      switch (directive) {
        case LOCAL_ONLY:
          uri += "?isLocal";
          break;
        case REMOTE_ONLY:
          uri += "?isRemote";
          break;
        case GROUP_ONLY:
          uri += "?asGroupOnly";
          break;
        case AS_EXPIRED:
          uri += "?asExpired";
          break;
        default:
          break;
      }
    }
    return uri;
  }

  @Override
  public boolean exists(final Location location) {
    return exists(location, toUri(location, null));
  }

  @Override
  public boolean existsWith(final Location location, final Directive directive) {
    return exists(location, toUri(location, directive));
  }

  protected boolean exists(final Location location, final String uri) {
    try {
      final ClientResponse response = getNexusClient().uri(uri).head();
      if (!ClientResponse.Status.OK.equals(response.getClientResponseStatus())) {
        if (ClientResponse.Status.NOT_FOUND.equals(response.getClientResponseStatus())) {
          return false;
        }

        throw getNexusClient().convert(new ContextAwareUniformInterfaceException(response)
        {
          @Override
          public String getMessage(final int status) {
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
              return String.format("Inexistent path: %s", location);
            }
            return null;
          }
        });
      }
      return true;
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public void download(final Location location, final File target) throws IOException {
    download(location, toUri(location, null), target, null);
  }

  @Override
  public void downloadRange(final Location location, final File target, final Range<Long> range) throws IOException {
    download(location, toUri(location, null), target, range);
  }

  @Override
  public void downloadRange(final Location location, final OutputStream stream, final Range<Long> range)
      throws IOException
  {
    download(location, toUri(location, null), stream, range);
  }

  @Override
  public void downloadWith(final Location location, final Directive directive, final File target) throws IOException {
    download(location, toUri(location, directive), target, null);
  }

  @Override
  public void downloadWith(Location location, Directive directive, OutputStream target) throws IOException {
    download(location, toUri(location, directive), target, null);
  }

  protected void download(final Location location, final String uri, final File target, final Range<Long> range)
      throws IOException
  {
    if (!target.exists()) {
      final File targetDir = target.getParentFile();
      checkState((targetDir.exists() || targetDir.mkdirs()) && targetDir.isDirectory(),
          "Directory '%s' does not exist and could not be created", targetDir.getAbsolutePath());
    }
    else {
      checkState(target.isFile() && target.canWrite(), "File '%s' is not a file or could not be written",
          target.getAbsolutePath());
    }

    OutputStream os = null;
    try {
      os = new BufferedOutputStream(new FileOutputStream(target));
      download(location, uri, os, range);
    }
    finally {
      IOUtil.close(os);
    }
  }

  protected void download(final Location location, final String uri, final OutputStream target, final Range<Long> range)
      throws IOException
  {
    try {
      final WebResource.Builder wrb = getNexusClient().uri(uri);
      if (range != null && (range.hasLowerBound() || range.hasUpperBound())) {
        final StringBuilder rangeHeaderValue = new StringBuilder("bytes=");
        if (range.hasLowerBound()) {
          rangeHeaderValue.append(range.lowerEndpoint());
        }
        rangeHeaderValue.append("-");
        if (range.hasUpperBound()) {
          rangeHeaderValue.append(range.upperEndpoint());
        }
        wrb.header("Range", rangeHeaderValue);
      }
      final ClientResponse response = wrb.get(ClientResponse.class);

      if (!(Status.OK.equals(response.getClientResponseStatus()) || Status.PARTIAL_CONTENT
          .equals(response.getClientResponseStatus()))) {
        throw getNexusClient().convert(new ContextAwareUniformInterfaceException(response)
        {
          @Override
          public String getMessage(final int status) {
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
              return String.format("Inexistent path: %s", location);
            }
            return null;
          }
        });
      }

      try {
        IOUtil.copy(response.getEntityInputStream(), target);
      }
      finally {
        response.close();
      }
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public void upload(final Location location, final File target) throws IOException {
    try {
      getNexusClient().uri(CONTENT_PREFIX + location.toContentPath()).put(target);
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(new ContextAwareUniformInterfaceException(e.getResponse())
      {
        @Override
        public String getMessage(final int status) {
          if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            return String.format("Inexistent path: %s", location);
          }
          return null;
        }
      });
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public void delete(final Location location) throws IOException {
    try {
      getNexusClient().uri(CONTENT_PREFIX + location.toContentPath()).delete();
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(new ContextAwareUniformInterfaceException(e.getResponse())
      {
        @Override
        public String getMessage(final int status) {
          if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            return String.format("Inexistent path: %s", location);
          }
          return null;
        }
      });
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

}
