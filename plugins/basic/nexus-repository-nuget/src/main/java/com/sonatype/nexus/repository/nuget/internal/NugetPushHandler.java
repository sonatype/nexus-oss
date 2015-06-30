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
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.google.common.base.Charsets;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A handler for getting and putting NuGet packages.
 *
 * @since 3.0
 */
public class NugetPushHandler
    extends AbstractNugetHandler
{
  private static final String EMPTY_HTMLDOC = "<html><body></body></html>";

  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final Request request = context.getRequest();
    final String action = request.getAction();
    try {
      switch (action) {
        case HttpMethods.PUT:
          return push(context, request);
        default:
          return HttpResponses.methodNotAllowed(action, HttpMethods.PUT);
      }
    }
    catch (Exception e) {
      return convertToXmlError(e);
    }
  }

  private Response push(@Nonnull final Context context, final Request request)
      throws IOException, NugetPackageException
  {
    checkArgument(request.isMultipart(), "Multipart request required");

    boolean created = false;
    final Iterable<Payload> multiparts = request.getMultiparts();
    for (Payload payload : multiparts) {
      storePayload(context, payload);
      created = true;
    }
    checkArgument(created, "No content was provided");

    return HttpResponses.created(new StringPayload(EMPTY_HTMLDOC, Charsets.UTF_8, "text/html"));
  }

  private void storePayload(final Context context, final Payload payload) throws IOException, NugetPackageException
  {
    final NugetGalleryFacet facet = context.getRepository().facet(NugetGalleryFacet.class);

    try (InputStream payloadInputStream = payload.openInputStream()) {
      facet.put(payloadInputStream);
    }
  }
}
