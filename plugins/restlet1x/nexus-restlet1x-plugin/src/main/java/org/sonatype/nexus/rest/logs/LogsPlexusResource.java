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
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.apache.commons.lang.StringUtils;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * The log file resource handler. It returns the content of the requested log file on incoming GET methods.
 *
 * @author cstamas
 */
@Component(role = PlexusResource.class, hint = "logs")
@Path(LogsPlexusResource.RESOURCE_URI)
@Produces({"text/plain"})
public class LogsPlexusResource
    extends AbstractNexusPlexusResource
{
  /**
   * Key for retrieving the requested filename from request.
   */
  public static final String FILE_NAME_KEY = "fileName";

  public static final String RESOURCE_URI = "/logs/{" + FILE_NAME_KEY + "}";

  /**
   * The LogFile Manager
   */
  @Requirement
  private LogManager logManager;

  @Override
  public List<Variant> getVariants() {
    return Collections.singletonList(new Variant(MediaType.TEXT_PLAIN));
  }

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
    return new PathProtectionDescriptor("/logs/*", "authcBasic,perms[nexus:logs]");
  }

  /**
   * The default handler. It simply extracts the requested file name and gets the file's InputStream from Nexus
   * instance. If Nexus finds the file appropriate, the handler wraps it into InputStream representation and ships it
   * as "text/plain" media type, otherwise HTTP 404 is returned.
   *
   * @param fileName The file name to retrieve (as defined in the log list resource response).
   */
  @Override
  @GET
  @ResourceMethodSignature(pathParams = {@PathParam(LogsPlexusResource.FILE_NAME_KEY)}, output = Object.class)
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    String logFile = request.getAttributes().get(FILE_NAME_KEY).toString();

    Form params = request.getResourceRef().getQueryAsForm();

    String fromStr = params.getFirstValue("from");

    String countStr = params.getFirstValue("count");

    long from = 0;

    long count = Long.MAX_VALUE;

    if (!StringUtils.isEmpty(fromStr)) {
      from = Long.valueOf(fromStr);
    }

    if (!StringUtils.isEmpty(countStr)) {
      count = Long.valueOf(countStr);
    }

    NexusStreamResponse result;
    try {
      result = logManager.getApplicationLogAsStream(logFile, from, count);
    }
    catch (IOException e) {
      throw new ResourceException(e);
    }

    if (result != null) {
      return new InputStreamRepresentation(MediaType.valueOf(result.getMimeType()), result.getInputStream());
    }
    else {
      getLogger().warn("Log file not found, filename=" + logFile);

      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Log file not found");
    }
  }
}
