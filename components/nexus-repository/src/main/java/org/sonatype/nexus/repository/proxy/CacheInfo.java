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
package org.sonatype.nexus.repository.proxy;

import java.util.Date;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.storage.Asset;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.time.DateHelper.toDate;
import static org.sonatype.nexus.common.time.DateHelper.toDateTime;

/**
 * A proxy cache auxiliary info for given element. {@link ProxyFacetSupport} relies on information provided by this
 * class to implement "aging" and proxy cache invalidation.
 *
 * @since 3.0
 */
public class CacheInfo
{
  private static final String P_PROXY_CACHE = "proxy_cache";

  private static final String P_LAST_VERIFIED = "last_verified";

  private static final String P_CACHE_TOKEN = "cache_token";

  private final DateTime lastVerified;

  @Nullable
  private final String cacheToken;

  public CacheInfo(final DateTime lastVerified, @Nullable final String cacheToken) {
    this.lastVerified = checkNotNull(lastVerified);
    this.cacheToken = cacheToken;
  }

  /**
   * Returns the {@link DateTime} when this item was last checked remotely for new version.
   */
  public DateTime getLastVerified() {
    return lastVerified;
  }

  /**
   * Returns the "cache token" that was in effect when this item was last checked remotely for new version.
   */
  @Nullable
  public String getCacheToken() {
    return cacheToken;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "lastVerified=" + lastVerified +
        ", cacheToken='" + cacheToken + '\'' +
        '}';
  }

  /**
   * Helper method that extracts {@link CacheInfo} from an {@link Asset}. Input cannot be {@code null}, but returned
   * value might be, if passed in {@link Asset} does not contain cache-info properties.
   */
  @Nullable
  public static CacheInfo extract(final Asset asset) {
    checkNotNull(asset);
    final NestedAttributesMap proxyCache = asset.attributes().child(P_PROXY_CACHE);
    final DateTime lastVerified = toDateTime(proxyCache.get(P_LAST_VERIFIED, Date.class));
    if (lastVerified == null) {
      return null;
    }
    final String cacheToken = proxyCache.get(P_CACHE_TOKEN, String.class);
    return new CacheInfo(lastVerified, cacheToken);
  }

  /**
   * Helper method that applies {@link CacheInfo} to an {@link Asset}. None of the inputs may be {@code null}.
   */
  public static void apply(final Asset asset, final CacheInfo cacheInfo) {
    checkNotNull(asset);
    checkNotNull(cacheInfo);
    final NestedAttributesMap proxyCache = asset.attributes().child(P_PROXY_CACHE);
    proxyCache.set(P_LAST_VERIFIED, toDate(cacheInfo.getLastVerified()));
    proxyCache.set(P_CACHE_TOKEN, cacheInfo.getCacheToken());
  }
}
