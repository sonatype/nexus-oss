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
package org.sonatype.nexus.repository.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;

import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public abstract class ProxyFacetSupport
    extends FacetSupport
    implements ProxyFacet
{
  public static final String CONFIG_KEY = "proxy";

  private URI remoteUrl;

  private int artifactMaxAgeMinutes;

  private HttpClientFacet httpClient;

  @Override
  protected void doConfigure() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    String url = attributes.require("remoteUrl", String.class);
    if (!url.endsWith("/")) {
      url = url + "/";
    }

    final URI newRemoteURI = new URI(url);
    if (remoteUrl != null && !remoteUrl.equals(newRemoteURI)) {
      log.debug("Remote URL is changing: clearing caches.");
      // TODO: Trigger other changes based on the remoteUrl changing - perhaps it calls facet(NFC.class).clear() at this point?
    }

    this.remoteUrl = newRemoteURI;
    log.debug("Remote URL: {}", remoteUrl);

    artifactMaxAgeMinutes = attributes.require("artifactMaxAge", Integer.class);
    log.debug("Artifact max age: {}", artifactMaxAgeMinutes);
  }


  @Override
  protected void doStart() throws Exception {
    httpClient = getRepository().facet(HttpClientFacet.class);
  }

  @Override
  protected void doStop() throws Exception {
    httpClient = null;
  }

  @Override
  protected void doDestroy() throws Exception {
    remoteUrl = null;
  }

  @Override
  public Payload get(final Context context) throws IOException {
    checkNotNull(context);

    Payload content = getCachedPayload(context);

    if (content == null || isStale(context)) {
      try {
        final Payload remote = fetch(context);
        if (remote != null) {

          // TODO: Introduce content validation.. perhaps content's type not matching path's implied type.

          store(context, remote);

          content = remote;
        }
      }
      catch (IOException e) {
        log.warn("Failed to fetch: {}", getUrl(context), e);
      }
    }
    return content;
  }

  /**
   * If we have the content cached locally already, return that - otherwise {@code null}.
   */
  protected abstract Payload getCachedPayload(final Context context) throws IOException;

  /**
   * Store a new Payload, freshly fetched from the remote URL. The Context indicates which component
   * was being requested.
   */
  protected abstract void store(final Context context, final Payload payload)
      throws IOException, InvalidContentException;

  @Nullable
  protected Payload fetch(final Context context) throws IOException {
    HttpClient client = httpClient.getHttpClient();

    HttpGet request = new HttpGet(remoteUrl.resolve(getUrl(context)));
    log.debug("Fetching: {}", request);

    HttpResponse response = client.execute(request);
    log.debug("Response: {}", response);

    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);

    Payload payload = null;
    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      try {
        log.debug("Entity: {}", entity);
        final HttpEntityPayload httpEntityPayload = new HttpEntityPayload(response, entity);

        payload = readFully(httpEntityPayload);
      }
      finally {
        EntityUtils.consume(entity);
      }
    }
    else if (status.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      indicateUpToDate(context);
    }

    return payload;
  }

  /**
   * For whatever component/asset is implied by the Context, return the date it was last deemed up to date, or {@code
   * null} if it isn't present.
   */
  protected abstract DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException;

  /**
   * For whatever component/asset
   */
  protected abstract void indicateUpToDate(final Context context) throws IOException;

  /**
   * Provide the relative URL to the
   */
  protected abstract String getUrl(final @Nonnull Context context);

  /**
   * Read an incoming Payload into memory.
   */
  private Payload readFully(final Payload payload) throws IOException {
    try (InputStream stream = payload.openInputStream()) {
      return new BytesPayload(ByteStreams.toByteArray(stream), payload.getContentType());
    }
  }

  private boolean isStale(final Context context) throws IOException {
    if (artifactMaxAgeMinutes < 0) {
      log.trace("Artifact max age checking disabled.");
      return false;
    }

    final DateTime lastUpdated = getCachedPayloadLastUpdatedDate(context);

    if (lastUpdated == null) {
      log.debug("Artifact last modified date unknown.");
      return true;
    }

    final DateTime earliestFreshDate = new DateTime().minusMinutes(artifactMaxAgeMinutes);
    return lastUpdated.isBefore(earliestFreshDate);
  }
}
