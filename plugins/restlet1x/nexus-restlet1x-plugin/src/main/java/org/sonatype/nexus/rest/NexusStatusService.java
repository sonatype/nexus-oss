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

package org.sonatype.nexus.rest;

import java.util.HashMap;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.service.StatusService;

/**
 * Nexus specific status service that simply assembles an "error page" out of a Velocity template but watching to HTML
 * escape any content that might come from external (ie. query param).
 *
 * @author cstamas
 */
@Component(role = StatusService.class)
public class NexusStatusService
    extends StatusService
{
  @Requirement
  private ReferenceFactory referenceFactory;

  @Requirement
  private ApplicationStatusSource applicationStatusSource;

  public Representation getRepresentation(final Status status, final Request request, final Response response) {
    final HashMap<String, Object> dataModel = new HashMap<String, Object>();

    final SystemStatus systemStatus = applicationStatusSource.getSystemStatus();
    dataModel.put("request", request);
    dataModel.put("nexusVersion", systemStatus.getVersion());
    // getContentRoot(req) always returns Reference with "/" as last character
    String nexusRoot = referenceFactory.getContextRoot(request).toString();
    if (nexusRoot.endsWith("/")) {
      nexusRoot = nexusRoot.substring(0, nexusRoot.length() - 1);
    }
    dataModel.put("nexusRoot", nexusRoot);

    dataModel.put("statusCode", status.getCode());
    dataModel.put("statusName", status.getName());
    dataModel.put("errorDescription", StringEscapeUtils.escapeHtml(status.getDescription()));

    if (null != status.getThrowable()) {
      dataModel.put("errorStackTrace",
          StringEscapeUtils.escapeHtml(ExceptionUtils.getStackTrace(status.getThrowable())));
    }

    final VelocityRepresentation representation =
        new VelocityRepresentation(Context.getCurrent(), "/templates/errorPageContentHtml.vm",
            getClass().getClassLoader(), dataModel, MediaType.TEXT_HTML);

    return representation;
  }
}
