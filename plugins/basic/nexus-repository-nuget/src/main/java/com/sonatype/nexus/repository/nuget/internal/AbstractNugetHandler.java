/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal;

import java.io.IOException;
import java.util.Map;

import com.sonatype.nexus.repository.nuget.odata.ODataTemplates;

import org.sonatype.nexus.repository.IllegalOperationException;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Base class for nuget handlers.
 *
 * @since 3.0
 */
abstract class AbstractNugetHandler
    extends ComponentSupport
    implements Handler
{
  protected Response convertToXmlError(final Exception e) {
    if (e instanceof NugetPackageException) {
      log.debug("Invalid package being uploaded", e);
      return xmlErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    else if (e instanceof IllegalArgumentException) {
      log.debug("Bad argument", e);
      return xmlErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    else if (e instanceof IllegalOperationException) {
      log.warn("Illegal operation", e);
      return xmlErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    else if (e instanceof IOException) {
      log.warn("I/O exception", e);
      return xmlErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    else {
      log.error("Unknown error", e);
      return xmlErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  protected Response xmlErrorMessage(final int code, final String message) {
    return xmlPayload(code, populateErrorTemplate(code, message));
  }

  protected Response xmlPayload(final int code, final String content) {
    final StringPayload stringPayload = new StringPayload(content, Charsets.UTF_8, ContentTypes.APPLICATION_XML);
    final Status status = code < 300 ? Status.success(code) : Status.failure(code);
    return new Response.Builder()
        .status(status)
        .payload(stringPayload)
        .build();
  }

  public String populateErrorTemplate(final int code, final String message) {
    final Map<String, String> data = ImmutableMap.of("CODE", Integer.toString(code), "MESSAGE", nullToEmpty(message));
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ERROR, data);
  }

  protected String getRepositoryBase(final Context context) {
    return context.getRepository().getUrl();
  }
}
