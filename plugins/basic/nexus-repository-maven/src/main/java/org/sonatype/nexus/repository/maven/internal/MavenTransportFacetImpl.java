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
package org.sonatype.nexus.repository.maven.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.maven.internal.Path.HashType;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
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
 * Implementation of {@link MavenTransportFacet}.
 *
 * @since 3.0
 */
@Named
public class MavenTransportFacetImpl
    extends FacetSupport
    implements MavenTransportFacet
{
  @Override
  public MavenPayload fetch(final Path path, final URI url, final MavenPayload stalePayload) throws IOException
  {
    checkNotNull(path);
    checkNotNull(url);

    final HttpClient client = getRepository().facet(HttpClientFacet.class).getHttpClient();
    final HttpGet request = new HttpGet(url);
    if (stalePayload != null) {
      request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, DateUtils.formatDate(stalePayload.getLastModified().toDate()));
      // request.addHeader(HttpHeaders.IF_NONE_MATCH, "{SHA1{" + stalePayload.getHashCode(HashAlgorithm.SHA1).toString() + "}}");
    }
    log.debug("Fetching: {}", request);

    HttpResponse response = client.execute(request);
    log.debug("Response: {}", response);

    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);

    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      log.debug("Entity: {}", entity);
      HttpEntityPayload payload = new HttpEntityPayload(response, entity);
      String sha1 = extractSha1(response.getLastHeader(HttpHeaders.ETAG));
      String md5 = null;
      if (!path.isHash()) {
        // if hash, do not attempt to download hash-of-a-hash
        if (sha1 == null) {
          sha1 = fetchHashCode(client, url, HashType.SHA1);
        }
        md5 = sha1 == null ? fetchHashCode(client, url, HashType.MD5) : null;
      }

      final Map<HashAlgorithm, HashCode> hashCodes = Maps.newHashMap();
      if (!Strings.isNullOrEmpty(sha1)) {
        hashCodes.put(HashAlgorithm.SHA1, HashCode.fromString(sha1));
      }
      if (!Strings.isNullOrEmpty(md5)) {
        hashCodes.put(HashAlgorithm.MD5, HashCode.fromString(md5));
      }
      final DateTime lastModified = extractLastModified(response.getLastHeader(HttpHeaders.LAST_MODIFIED));
      return new MavenPayload(payload, new DateTime(), lastModified, hashCodes);
    }
    else if (status.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      return stalePayload;
    }
    return null;
  }

  /**
   * Extracts SHA1 from "NX style" ETag header.
   */
  private String extractSha1(final Header etagHeader) {
    if (etagHeader != null) {
      final String etag = etagHeader.getValue();
      if (!Strings.isNullOrEmpty(etag)) {
        int start = etag.indexOf("SHA1{"), end = etag.indexOf("}", start + 5);
        if (start >= 0 && end > start) {
          final String sha1 = etag.substring(start + 5, end);
          if (DigestExtractor.isDigest(sha1)) {
            return sha1;
          }
        }
      }
    }
    return null;
  }

  /**
   * Extract last-modified date from response if possible, or "now".
   */
  private DateTime extractLastModified(final Header lastModifiedHeader) {
    DateTime result = new DateTime();
    if (lastModifiedHeader != null) {
      try {
        result = new DateTime(DateUtils.parseDate(lastModifiedHeader.getValue()).getTime());
      }
      catch (Exception ex) {
        log.warn("Could not parse date '{}', using system current time as item creation time.", lastModifiedHeader, ex);
      }
    }
    return result;
  }

  /**
   * Fetches unconditionally Maven specific hash files from remote.
   */
  private String fetchHashCode(final HttpClient httpClient, final URI url, final HashType hashType) throws IOException {
    final HttpGet request = new HttpGet(url + "." + hashType.getExt());
    log.debug("Fetching {}: {}", hashType, request);
    HttpResponse response = httpClient.execute(request);
    log.debug("Response: {}", response);
    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);
    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      log.debug("Entity: {}", entity);
      return DigestExtractor.extract(new HttpEntityPayload(response, entity));
    }
    else {
      return null;
    }
  }
}
