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
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.sonatype.nexus.repository.nuget.odata.FeedSplicer;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Talks to an upstream, proxied Nuget repository to download counts or feeds of available entries to be stored in a
 * nuget gallery.
 *
 * @since 3.0
 */
@Named
public class NugetFeedFetcher
    extends ComponentSupport
{
  /**
   * Caches the NuGet feed from the given proxy repository.  Returns the number of items that satisfied the query.
   *
   * @param proxy Proxy repository
   */
  @Nullable
  public Integer cachePackageFeed(final Repository proxy, final URI nugetQuery,
                                  final boolean followNextPageLinks, final ODataConsumer odataConsumer)
      throws IOException
  {
    checkNotNull(proxy);
    checkNotNull(nugetQuery);
    checkNotNull(odataConsumer);

    final FeedSplicer splicer = new FeedSplicer(odataConsumer);

    final Set<String> visited = new LinkedHashSet<>();

    URI remoteUrl = absoluteURI(proxy, nugetQuery);

    do {
      // download and cache results, following 'next' links if requested
      final Payload payload = getPayload(proxy, remoteUrl);

      if (payload == null) {
        // The request returned no XML. Return whatever the splicer learned on the last correctly processed
        // page of XML.
        return splicer.getCount();
      }

      try (InputStream is = payload.openInputStream()) {
        remoteUrl = parseFeed(is, splicer, visited, followNextPageLinks);
      }
    }
    while (remoteUrl != null);

    return splicer.getCount();
  }

  /**
   * Consume a page of feed XML from the InputStream.
   *
   * @return The URI for the next (unvisited) page, if there is one.
   */
  private URI parseFeed(final InputStream is, final FeedSplicer splicer, final Set<String> visited,
                        final boolean followNextPageLinks) throws IOException
  {
    try {
      final String nextPageUrl = splicer.consumePage(is);

      if (followNextPageLinks && nextPageUrl != null) {
        if (visited.add(nextPageUrl)) {
          return new URI(nextPageUrl);
        }
        else {
          log.warn("Page cycle detected: {} -> {}", visited, nextPageUrl);
        }
      }
      return null;
    }
    catch (XmlPullParserException e) {
      throw new IOException("Invalid nuget feed XML received", e);
    }
    catch (URISyntaxException e) {
      throw new IOException("Invalid 'next page' URI in nuget XML feed", e);
    }
  }

  /**
   * Pass a count-style Nuget query to a remote repository.
   */
  @Nullable
  public Integer getCount(final Repository proxy, URI nugetQuery) throws IOException {
    final Payload item = getPayload(proxy, absoluteURI(proxy, nugetQuery));
    if (item == null) {
      return 0;
    }

    try (InputStream is = item.openInputStream()) {
      final String s = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)).trim();
      return Integer.parseInt(s);
    }
  }

  private URI absoluteURI(final Repository proxy, final URI nugetQuery) {
    final URI repoBaseUrl = proxy.facet(ProxyFacet.class).getRemoteUrl();
    return repoBaseUrl.resolve(nugetQuery);
  }

  private Payload getPayload(final Repository proxy, final URI uri) throws IOException
  {
    final HttpClient client = proxy.facet(HttpClientFacet.class).getHttpClient();

    HttpGet request = new HttpGet(uri);
    log.debug("Fetching: {}", request);

    HttpResponse response = client.execute(request);
    log.debug("Response: {}", response);

    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);

    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      checkState(entity != null, "No http entity received from remote nuget query");

      log.debug("Entity: {}", entity);
      return new HttpEntityPayload(response, entity);
    }
    else {
      log.warn("Status code {} contacting {}", status.getStatusCode(), uri);
    }
    HttpClientUtils.closeQuietly(response);
    return null;
  }
}
