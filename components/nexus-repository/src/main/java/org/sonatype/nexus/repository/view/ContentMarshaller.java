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
package org.sonatype.nexus.repository.view;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.proxy.CacheInfo;
import org.sonatype.nexus.repository.proxy.CacheInfoMarshaller;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.time.DateHelper.toDate;
import static org.sonatype.nexus.common.time.DateHelper.toDateTime;

/**
 * Extracts non-format specific auxiliary metadata from asset.
 *
 * @since 3.0
 */
public class ContentMarshaller
{
  /**
   * Key of the nested map on {@link Asset} attributes carrying content related attributes.
   */
  private static final String P_CONTENT_ATTRIBUTES = "contentAttributes";

  /**
   * Key of last modified {@link Date}.
   */
  private static final String P_LAST_MODIFIED = "last_modified";

  /**
   * Key of etag {@link String}. If extracted from upstream response, it must have quotes removed.
   */
  private static final String P_ETAG = "etag";

  private ContentMarshaller() {}

  /**
   * Extracts non-format specific content attributes into the passed in {@link AttributesMap} (usually originating from
   * {@link Content#getAttributes()}) from passed in {@link Asset} and format required hashes.
   */
  public static void extractFromAsset(final Asset asset,
                                      final Iterable<HashAlgorithm> hashAlgorithms,
                                      final AttributesMap contentAttributes)
  {
    checkNotNull(asset);
    checkNotNull(hashAlgorithms);
    final NestedAttributesMap assetAttributes = asset.attributes().child(P_CONTENT_ATTRIBUTES);
    final DateTime lastModified = toDateTime(assetAttributes.get(P_LAST_MODIFIED, Date.class));
    final String etag = assetAttributes.get(P_ETAG, String.class);

    final NestedAttributesMap checksumAttributes = asset.attributes().child(StorageFacet.P_CHECKSUM);
    final Map<HashAlgorithm, HashCode> hashCodes = Maps.newHashMap();
    for (HashAlgorithm algorithm : hashAlgorithms) {
      final HashCode hashCode = HashCode.fromString(checksumAttributes.require(algorithm.name(), String.class));
      hashCodes.put(algorithm, hashCode);
    }

    contentAttributes.set(Content.CONTENT_LAST_MODIFIED, lastModified);
    contentAttributes.set(Content.CONTENT_ETAG, etag);
    contentAttributes.set(Content.CONTENT_HASH_CODES_MAP, hashCodes);
    contentAttributes.set(CacheInfo.class, CacheInfoMarshaller.extractFromAsset(asset));
  }

  /**
   * Applies non-format specific content attributes onto passed in {@link Asset} from passed in {@link AttributesMap}
   * (usually originating from {@link Content#getAttributes()}).
   */
  public static void applyToAsset(final Asset asset, final AttributesMap contentAttributes) {
    checkNotNull(asset);
    checkNotNull(contentAttributes);
    final NestedAttributesMap assetAttributes = asset.attributes().child(P_CONTENT_ATTRIBUTES);
    assetAttributes.set(P_LAST_MODIFIED, toDate(contentAttributes.get(Content.CONTENT_LAST_MODIFIED, DateTime.class)));
    assetAttributes.set(P_ETAG, contentAttributes.get(Content.CONTENT_ETAG, String.class));
    final CacheInfo cacheInfo = contentAttributes.get(CacheInfo.class);
    if (cacheInfo != null) {
      CacheInfoMarshaller.applyToAsset(asset, cacheInfo);
    }
  }

  /**
   * Maintains the "last modified" attribute of the content by setting it to "now". It accepts {@code null}s, and the
   * returned instance will have changes applied. Never returns {@code null}.
   */
  @Nonnull
  public static AttributesMap maintainLastModified(final Asset asset,
                                                   @Nullable final AttributesMap contentAttributes)
  {
    AttributesMap ca = contentAttributes;
    if (ca == null) {
      ca = new AttributesMap();
    }
    if (!ca.contains(Content.CONTENT_LAST_MODIFIED)) {
      ca.set(Content.CONTENT_LAST_MODIFIED, DateTime.now());
    }
    return ca;
  }
}
