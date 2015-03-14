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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.Iso8601Date;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.net.HttpHeaders;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;

/**
 * Maven specific payload that enriches {@link Payload}.
 *
 * @since 3.0
 */
public class MavenPayload
    implements Payload
{
  /**
   * List of "supported" hash algorithms. Maven payload will always have these.
   */
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA1, MD5);

  private final Payload payload;

  private final DateTime lastUpdated;

  private final DateTime lastModified;

  private final Map<HashAlgorithm, HashCode> hashCodes;

  public MavenPayload(final Payload payload,
                      final DateTime lastUpdated,
                      final DateTime lastModified,
                      final Map<HashAlgorithm, HashCode> hashCodes)
  {
    this.payload = checkNotNull(payload);
    this.lastModified = checkNotNull(lastModified);
    this.lastUpdated = checkNotNull(lastUpdated);
    this.hashCodes = checkNotNull(hashCodes);
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return payload.openInputStream();
  }

  @Override
  public long getSize() {
    return payload.getSize();
  }

  @Override
  public String getContentType() {
    return payload.getContentType();
  }

  /**
   * If this instance is read from {@link MavenFacet} then the returned value will represent when the underlying
   * structures (internals of storage) did change last time. This is NOT like a file last modified timestamp, this is
   * only for internal use.
   */
  @Nonnull
  public DateTime getLastUpdated() {
    return lastUpdated;
  }

  /**
   * The file "last modified" timestamp alike value, shows when did the underlying content last changed.
   */
  @Nonnull
  public DateTime getLastModified() {
    return lastModified;
  }

  /**
   * Returns the set of {@link HashAlgorithm} this instance has filled in.
   */
  @Nonnull
  public Set<HashAlgorithm> getHashAlgorithms() {
    return hashCodes.keySet();
  }

  /**
   * Returns the {@link HashCode} corresponding to given algorithm, if any.
   */
  @Nullable
  public HashCode getHashCode(final HashAlgorithm algorithm) {
    checkNotNull(algorithm);
    return hashCodes.get(algorithm);
  }

  // ==

  public static Response ok(final MavenPayload payload) {
    final Response response = HttpResponses.ok(payload);
    final HashCode sha1 = payload.getHashCode(HashAlgorithm.SHA1);
    checkArgument(sha1 != null); // sanity
    // TODO: this clearly creeps HTTP protocol in here, Date formatting and headers
    response.getHeaders().set(HttpHeaders.ETAG, "{SHA1{" + sha1 + "}}");
    response.getHeaders().set(HttpHeaders.LAST_MODIFIED, Iso8601Date.format(payload.getLastModified().toDate()));
    return response;
  }
}
