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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.log.DefaultLogConfiguration;
import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * @author juven
 * @author adreghiciu@gmail.com
 */
@Component(role = PlexusResource.class, hint = "logConfig")
@Path(LogConfigPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class LogConfigPlexusResource
    extends AbstractNexusPlexusResource
{
  public static final String RESOURCE_URI = "/log/config";

  /**
   * The LogFile Manager
   */
  @Requirement
  private LogManager logManager;

  public LogConfigPlexusResource() {
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new LogConfigResourceResponse();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:logconfig]");
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  /**
   * Get the logging configuration.
   */
  @Override
  @GET
  @ResourceMethodSignature(output = LogConfigResourceResponse.class)
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    LogConfigResourceResponse result = new LogConfigResourceResponse();

    try {
      LogConfiguration configuration = logManager.getConfiguration();

      LogConfigResource data = new LogConfigResource();

      data.setRootLoggerLevel(configuration.getRootLoggerLevel());

      data.setRootLoggerAppenders(configuration.getRootLoggerAppenders());

      data.setFileAppenderLocation(configuration.getFileAppenderLocation());

      data.setFileAppenderPattern(configuration.getFileAppenderPattern());

      result.setData(data);

      return result;
    }
    catch (IOException e) {
      getLogger().warn("Could not load log configuration!", e);

      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }
  }

  /**
   * Update the logging configuration.
   */
  @Override
  @PUT
  @ResourceMethodSignature(input = LogConfigResourceResponse.class, output = LogConfigResourceResponse.class)
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    LogConfigResourceResponse requestResource = (LogConfigResourceResponse) payload;

    if (requestResource == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    try {
      LogConfigResource data = requestResource.getData();

      DefaultLogConfiguration configuration = new DefaultLogConfiguration();

      configuration.setRootLoggerLevel(data.getRootLoggerLevel());

      configuration.setRootLoggerAppenders(data.getRootLoggerAppenders());

      configuration.setFileAppenderLocation(data.getFileAppenderLocation());

      configuration.setFileAppenderPattern(data.getFileAppenderPattern());

      logManager.setConfiguration(configuration);

      LogConfigResourceResponse responseResource = new LogConfigResourceResponse();

      responseResource.setData(data);

      return responseResource;
    }
    catch (IOException e) {
      getLogger().warn("Could not set log configuration!", e);

      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }
  }

}
