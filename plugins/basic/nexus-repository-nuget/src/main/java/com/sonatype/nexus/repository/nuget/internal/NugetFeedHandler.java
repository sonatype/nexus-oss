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

import java.util.Map;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

/**
 * @since 3.0
 */
public class NugetFeedHandler
    extends AbstractNugetHandler
{
  public static final String FEED_COUNT_PATTERN = "/{operation}()/$count";

  public static final String FEED_PATTERN = "/{operation}()";

  public static final String PACKAGE_ENTRY_PATTERN = "/Packages(Id='{id}',Version='{version}')";

  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final State state = context.getAttributes().get(State.class);
    final Map<String, String> tokens = state.getTokens();
    final NugetGalleryFacet facet = context.getRepository().facet(NugetGalleryFacet.class);
    final Parameters queryParameters = context.getRequest().getParameters();

    switch (state.pattern()) {
      case FEED_PATTERN:
        return feed(context, tokens, facet, queryParameters);

      case FEED_COUNT_PATTERN:
        return feedCount(context, facet, queryParameters);

      case PACKAGE_ENTRY_PATTERN:
        return packageEntry(context, tokens, facet);

      default:
        throw new IllegalStateException("Unexpected path pattern passed to " + getClass().getSimpleName());
    }
  }


  private Response feed(final @Nonnull Context context, final Map<String, String> tokens, final NugetGalleryFacet facet,
                        final Parameters queryParameters)
  {
    final String feed = facet.feed(getRepositoryBase(context), tokens.get("operation"), asMap(queryParameters));
    return xmlPayload(200, feed);
  }

  private Response feedCount(final @Nonnull Context context, final NugetGalleryFacet facet,
                             final Parameters queryParameters)
  {
    // Remove the leading slash to derive the operation
    final String operation = context.getRequest().getPath().substring(1);
    final int count = facet.count(operation, asMap(queryParameters));
    return HttpResponses.ok(new StringPayload(Integer.toString(count), Charsets.UTF_8, "text/plain"));
  }

  private Response packageEntry(final @Nonnull Context context, final Map<String, String> tokens,
                                final NugetGalleryFacet facet)
  {
    final String entry = facet.entry(getRepositoryBase(context), tokens.get("id"), tokens.get("version"));
    if (entry == null) {
      return HttpResponses.notFound();
    }
    return xmlPayload(200, entry);
  }

  public static Map<String, String> asMap(final Parameters parameters) {
    Map<String, String> query = Maps.newHashMap();
    for (String param : parameters.names()) {
      query.put(param, parameters.get(param));
    }
    return query;
  }
}
