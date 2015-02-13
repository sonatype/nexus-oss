/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nonnull;

import com.sonatype.nexus.repository.nuget.internal.odata.ODataTemplates;

import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * A handler for getting and putting NuGet packages.
 *
 * @since 3.0
 */
public class NugetContentHandler
    extends ComponentSupport
    implements Handler
{
  private static final String EMPTY_HTMLDOC = "<html><body></body></html>";

  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final Request request = context.getRequest();
    final String action = request.getAction();
    try {
      switch (action) {
        // TODO: Implement GET

        case HttpMethods.PUT:
          checkArgument(request.isMultipart(), "Multipart request required");

          boolean created = false;
          final Iterable<Payload> multiparts = request.getMultiparts();
          for (Payload payload : multiparts) {
            storePayload(context, payload);
            created = true;
          }
          checkArgument(created, "No content was provided");

          return HttpResponses.created(new StringPayload(EMPTY_HTMLDOC, Charsets.UTF_8, "text/html"));
        default:
          return HttpResponses.methodNotAllowed(action, HttpMethods.PUT /* TODO: , HttpMethods.GET */);
      }
    }
    catch (Exception e) {
      return convertToXmlError(e);
    }
  }

  private void storePayload(final Context context, final Payload payload) throws IOException, NugetPackageException
  {
    final NugetGalleryFacet facet = context.getRepository().facet(NugetGalleryFacet.class);

    try (InputStream payloadInputStream = payload.openInputStream()) {
      facet.put(payloadInputStream);
    }
  }

  private Response convertToXmlError(final Exception e) {
    if (e instanceof NugetPackageException) {
      log.debug("Invalid package being uploaded", e);
      return xmlErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    if (e instanceof IllegalArgumentException) {
      log.debug("Bad argument", e);
      return xmlErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    else if (e instanceof IOException) {
      log.warn("I/O exception", e);
      return xmlErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    else {
      log.error("Unknown error", e);
      return xmlErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Response xmlErrorResponse(final int code, final String message) {
    final StringPayload stringPayload = new StringPayload(populateErrorTemplate(code, message), Charsets.UTF_8,
        "application/xml");
    return new PayloadResponse(Status.failure(code), stringPayload);
  }

  public String populateErrorTemplate(final int code, final String message) {
    final Map<String, String> data = ImmutableMap.of("CODE", Integer.toString(code), "MESSAGE", nullToEmpty(message));
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ERROR, data);
  }
}
