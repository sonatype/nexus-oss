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
package com.sonatype.nexus.repository.nuget.internal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.sonatype.nexus.repository.nuget.internal.NugetGalleryFacet;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;

import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.joda.time.DateTime;

/**
 * Serves NuGet packages, and also exposes proxy configuration and content-fetching.
 *
 * @since 3.0
 */
public class NugetProxyFacet
    extends ProxyFacetSupport
{
  private final NugetFeedFetcher fetcher;

  @Inject
  public NugetProxyFacet(NugetFeedFetcher fetcher) {
    this.fetcher = fetcher;
  }

  @Override
  protected Content getCachedPayload(final Context context) throws IOException {
    String[] coords = coords(context);
    Payload payload = gallery().get(coords[0], coords[1]);
    if (payload != null) {
      // TODO: get last-modified and etag
      return new Content(payload);
    }
    return null;
  }

  @Override
  protected Content fetch(final Context context, final Content stale) throws IOException {
    // Here we override the default fetch behavior because we need to first fetch
    // the remote entry and cache it, then from that, determine the remote URL of
    // the actual package content, and get it.

    // Determine the remote entry URL
    String[] coords = coords(context);
    String suffix = "Packages(Id='" + coords[0] + "',Version='" + coords[1] + "')";
    URI remoteEntryUri = getRemoteUrl().resolve(suffix);

    // Cache the metadata from the remote, grabbing the content location as we go
    final StringBuilder contentLocation = new StringBuilder();
    fetcher.cachePackageFeed(getRepository(), remoteEntryUri, false, new ODataConsumer()
    {
      @Override
      public void consume(final Map<String, String> data) {
        if (contentLocation.length() == 0) {
          contentLocation.append(data.get("LOCATION"));
        }
        gallery().putMetadata(data);
      }
    });
    if (contentLocation.length() == 0) {
      throw new IOException("Package content not found in remote repository");
    }

    // Request the remote package content and return it as a payload
    return fetch(contentLocation.toString(), context, stale);
  }

  @Override
  protected void store(final Context context, final Content payload) throws IOException, InvalidContentException {
    // The metadata will have been cached by this time, so we just need to set the content
    String[] coords = coords(context);
    try (InputStream in = payload.openInputStream()) {
      gallery().putContent(coords[0], coords[1], in);
    }
  }

  @Override
  protected DateTime getCachedPayloadLastVerified(final Context context) throws IOException {
    String[] coords = coords(context);
    return gallery().getLastVerified(coords[0], coords[1]);
  }

  @Override
  protected void indicateVerified(final Context context) throws IOException {
    String[] coords = coords(context);
    gallery().setLastVerified(coords[0], coords[1], new DateTime());
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    // This is only implemented for error-reporting purposes. It returns the Nexus
    // URL for the content, rather than the remote one, as these can differ.
    final String[] coords = coords(context);
    return coords[0] + "/" + coords[1];
  }

  private NugetGalleryFacet gallery() {
    return getRepository().facet(NugetGalleryFacet.class);
  }

  private static String[] coords(Context context) {
    Map<String, String> tokens = context.getAttributes().require(TokenMatcher.State.class).getTokens();
    return new String[]{tokens.get("id"), tokens.get("version")};
  }
}
