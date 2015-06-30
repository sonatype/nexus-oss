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

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import com.google.common.annotations.VisibleForTesting;

/**
 * A handler for getting and putting NuGet packages.
 *
 * @since 3.0
 */
public class NugetItemHandler
    extends AbstractNugetHandler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) {
    final Request request = context.getRequest();
    final String action = request.getAction();
    final Map<String, String> tokens = getTokens(context);
    String id = tokens.get("id");
    String version = tokens.get("version");
    try {
      switch (action) {
        case HttpMethods.GET:
          return getItem(id, version, context);
        case HttpMethods.DELETE:
          return deleteItem(id, version, context);
        default:
          return HttpResponses.methodNotAllowed(action, HttpMethods.GET, HttpMethods.DELETE);
      }
    }
    catch (Exception e) {
      log.warn("Failed to handle", e);
      return convertToXmlError(e);
    }
  }

  @VisibleForTesting
  Response getItem(final String id, final String version, final Context context) throws IOException {
    NugetGalleryFacet facet = context.getRepository().facet(NugetGalleryFacet.class);
    Payload payload = facet.get(id, version);
    if (payload == null) {
      return xmlErrorMessage(HttpStatus.NOT_FOUND, String.format("No such package: id=%s, version=%s", id, version));
    }
    else {
      return HttpResponses.ok(payload);
    }
  }

  @VisibleForTesting
  Response deleteItem(final String id, final String version, final Context context) throws IOException {
    NugetGalleryFacet facet = context.getRepository().facet(NugetGalleryFacet.class);
    if (!facet.delete(id, version)) {
      return xmlErrorMessage(HttpStatus.NOT_FOUND, String.format("No such package: id=%s, version=%s", id, version));
    }
    return HttpResponses.noContent();
  }

  @VisibleForTesting
  Map<String, String> getTokens(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class).getTokens();
  }
}
