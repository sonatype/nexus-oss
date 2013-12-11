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

package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;

import org.sonatype.nexus.internal.DevModeResources;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebResource.CacheControl;
import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * {@link WebResource} contributed from a Nexus plugin.
 */
@Deprecated
public final class PluginWebResource
    implements WebResource, CacheControl
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final GAVCoordinate gav;

  private final URL resourceURL;

  private final String publishedPath;

  private final String contentType;

  private final boolean shouldCache;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public PluginWebResource(final GAVCoordinate gav,
                           final URL resourceURL,
                           final String publishedPath,
                           final String contentType)
  {
    URL overrideUrl = DevModeResources.getResourceIfOnFileSystem(publishedPath);
    this.gav = gav;
    this.resourceURL = overrideUrl != null ? overrideUrl : resourceURL;
    this.publishedPath = publishedPath;
    this.contentType = contentType;
    this.shouldCache = overrideUrl == null;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public String getPath() {
    return publishedPath;
  }

  public String getContentType() {
    return contentType;
  }

  public long getSize() {
    try {
      return resourceURL.openConnection().getContentLength();
    }
    catch (final Throwable e) // NOPMD
    {
      // default to unknown size
    }
    return -1;
  }

  public InputStream getInputStream()
      throws IOException
  {
    return resourceURL.openStream();
  }

  public Long getLastModified() {
    if (!shouldCache) {
      return System.currentTimeMillis();
    }
    try {
      final URLConnection urlConn = resourceURL.openConnection();
      if (urlConn instanceof JarURLConnection) {
        final JarEntry jarEntry = ((JarURLConnection) urlConn).getJarEntry();
        if (jarEntry != null) {
          return jarEntry.getTime();
        }
        // This is a jar, not an entry in a jar
      }
      return urlConn.getLastModified();
    }
    catch (final Throwable e) // NOPMD
    {
      return null; // default to unknown last modified time
    }
  }

  @Override
  public boolean shouldCache() {
    return shouldCache;
  }

  @Override
  public String toString() {
    return "PluginWebResource{" +
        "gav=" + gav +
        ", url=" + resourceURL +
        '}';
  }
}
