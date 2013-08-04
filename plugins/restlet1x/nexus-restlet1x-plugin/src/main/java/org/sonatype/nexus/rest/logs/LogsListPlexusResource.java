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

package org.sonatype.nexus.rest.logs;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * The log file list resource handler. This handles the GET method only and simply returns the list of existing nexus
 * application log files.
 *
 * @author cstamas
 */
@Component(role = PlexusResource.class, hint = "logsList")
@Path(LogsListPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
public class LogsListPlexusResource
    extends AbstractNexusPlexusResource
{
  /**
   * The LogFile Manager
   */
  @Requirement
  private LogManager logManager;

  public static final String RESOURCE_URI = "/logs";

  @Override
  public Object getPayloadInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:logs]");
  }

  /**
   * Get the list of log files on the server.
   */
  @Override
  @GET
  @ResourceMethodSignature(output = LogsListResourceResponse.class)
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    LogsListResourceResponse result = new LogsListResourceResponse();
    result.getData(); //just to load the data, prevent problem on js side

    try {
      Collection<NexusStreamResponse> logFiles = logManager.getApplicationLogFiles();

      for (NexusStreamResponse logFile : logFiles) {
        LogsListResource resource = new LogsListResource();

        resource.setResourceURI(createChildReference(request, this, logFile.getName()).toString());

        resource.setName(logFile.getName());

        resource.setSize(logFile.getSize());

        resource.setMimeType(logFile.getMimeType());

        result.addData(resource);
      }
    }
    catch (IOException e) {
      throw new ResourceException(e);
    }

    return result;
  }
}
