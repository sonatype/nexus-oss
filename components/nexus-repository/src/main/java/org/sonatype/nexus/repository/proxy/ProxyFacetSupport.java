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
import java.net.URI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.negativecache.NegativeCacheFacet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A support class which implements basic payload logic; subclasses provide format-specific operations.
 *
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

  private boolean remoteUrlChanged;

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    NestedAttributesMap attributes = configuration.attributes(CONFIG_KEY);
    String url = attributes.require("remoteUrl", String.class);
    if (!url.endsWith("/")) {
      url = url + "/";
    }

    final URI newRemoteURI = new URI(url);
    if (remoteUrl != null && !remoteUrl.equals(newRemoteURI)) {
      remoteUrlChanged = true;
    }

    this.remoteUrl = newRemoteURI;
    log.debug("Remote URL: {}", remoteUrl);

    artifactMaxAgeMinutes = attributes.require("artifactMaxAge", Integer.class);
    log.debug("Artifact max age: {}", artifactMaxAgeMinutes);
  }


  @Override
  protected void doStart() throws Exception {
    httpClient = getRepository().facet(HttpClientFacet.class);
    if (remoteUrlChanged) {
      remoteUrlChanged = false;
      try {
        getRepository().facet(NegativeCacheFacet.class).invalidate();
      }
      catch (MissingFacetException e) {
        // NCF is optional
      }
    }
  }

  @Override
  protected void doStop() throws Exception {
    httpClient = null;
  }

  @Override
  protected void doDestroy() throws Exception {
    remoteUrl = null;
  }

  public URI getRemoteUrl() {
    return remoteUrl;
  }

  @Override
  public Content get(final Context context) throws IOException {
    checkNotNull(context);

    Content content = getCachedPayload(context);

    if (content == null || isStale(context)) {
      try {
        final Content remote = fetch(context, content);
        if (remote != null) {

          // TODO: Introduce content validation.. perhaps content's type not matching path's implied type.

          store(context, remote);

          content = getCachedPayload(context);
        }
      }
      catch (IOException e) {
        log.warn("Failed to fetch: {}", getUrl(context), e);
        throw e;
      }
    }
    return content;
  }

  /**
   * If we have the content cached locally already, return that - otherwise {@code null}.
   */
  protected abstract Content getCachedPayload(final Context context) throws IOException;

  /**
   * Store a new Payload, freshly fetched from the remote URL. The Context indicates which component
   * was being requested.
   */
  protected abstract void store(final Context context, final Content content)
      throws IOException, InvalidContentException;

  @Nullable
  protected Content fetch(final Context context, Content stale) throws IOException {
    return fetch(getUrl(context), context, stale);
  }

  protected Content fetch(String url, Context context, Content stale) throws IOException {
    HttpClient client = httpClient.getHttpClient();

    HttpGet request = new HttpGet(remoteUrl.resolve(url));
    if (stale != null) {
      final DateTime lastModified = stale.getAttributes().get(Content.CONTENT_LAST_MODIFIED, DateTime.class);
      if (lastModified != null) {
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, DateUtils.formatDate(lastModified.toDate()));
      }
      final String etag = stale.getAttributes().get(Content.CONTENT_ETAG, String.class);
      if (etag != null) {
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"" + etag + "\"");
      }
    }
    log.debug("Fetching: {}", request);

    HttpResponse response = client.execute(request);
    log.debug("Response: {}", response);

    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);

    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      log.debug("Entity: {}", entity);

      Payload payload = new HttpEntityPayload(response, entity);
      final Content result = new Content(payload);
      result.getAttributes().set(Content.CONTENT_LAST_MODIFIED, extractLastModified(response.getLastHeader(HttpHeaders.LAST_MODIFIED)));
      result.getAttributes().set(Content.CONTENT_ETAG, extractETag(response.getLastHeader(HttpHeaders.ETAG)));
      return result;
    }
    if (status.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      indicateUpToDate(context);
    }

    return null;
  }

  /**
   * Extract Last-Modified date from response if possible, or {@code null}.
   */
  @Nullable
  private DateTime extractLastModified(final Header lastModifiedHeader) {
    if (lastModifiedHeader != null) {
      try {
        return new DateTime(DateUtils.parseDate(lastModifiedHeader.getValue()).getTime());
      }
      catch (Exception ex) {
        log.warn("Could not parse date '{}', using system current time as item creation time.", lastModifiedHeader, ex);
      }
    }
    return null;
  }

  /**
   * Extract ETag from response if possible, or {@code null}.
   */
  @Nullable
  private String extractETag(final Header etagHeader) {
    if (etagHeader != null) {
      final String etag = etagHeader.getValue();
      if (!Strings.isNullOrEmpty(etag)) {
        if (etag.startsWith("\"") && etag.endsWith("\"")) {
          return etag.substring(1, etag.length() - 1);
        }
        else {
          return etag;
        }
      }
    }
    return null;
  }

  /**
   * For whatever component/asset is implied by the Context, return the date it was last deemed up to date, or {@code
   * null} if it isn't present.
   */
  @Nullable
  protected abstract DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException;

  /**
   * For whatever component/asset
   */
  protected abstract void indicateUpToDate(final Context context) throws IOException;

  /**
   * Provide the relative URL to the
   */
  protected abstract String getUrl(final @Nonnull Context context);

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
