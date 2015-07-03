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

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.proxy.CacheInfo;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.time.DateHelper.toDate;
import static org.sonatype.nexus.common.time.DateHelper.toDateTime;

/**
 * Extracts non-format specific auxiliary matadata from asset.
 *
 * @since 3.0
 */
public class ContentMarshaller
{
  private static final String P_CONTENT = "content";

  private static final String P_LAST_MODIFIED = "last_modified";

  private static final String P_ETAG = "etag";

  private ContentMarshaller() {}

  /**
   * Extracts non-format specific content attributes into the passed in {@link AttributesMap} (usually originating from
   * {@link Content#getAttributes()}) from passed in {@link Asset} and format required hashes.
   */
  public static void extract(final AttributesMap content,
                             final Asset asset,
                             final Iterable<HashAlgorithm> hashAlgorithms)
  {
    checkNotNull(asset);
    checkNotNull(hashAlgorithms);
    final NestedAttributesMap contentAttributes = asset.attributes().child(P_CONTENT);
    final DateTime lastModified = toDateTime(contentAttributes.get(P_LAST_MODIFIED, Date.class));
    final String etag = contentAttributes.get(P_ETAG, String.class);

    final NestedAttributesMap checksumAttributes = asset.attributes().child(StorageFacet.P_CHECKSUM);
    final Map<HashAlgorithm, HashCode> hashCodes = Maps.newHashMap();
    for (HashAlgorithm algorithm : hashAlgorithms) {
      final HashCode hashCode = HashCode.fromString(checksumAttributes.require(algorithm.name(), String.class));
      hashCodes.put(algorithm, hashCode);
    }

    content.set(Content.CONTENT_LAST_MODIFIED, lastModified);
    content.set(Content.CONTENT_ETAG, etag);
    content.set(Content.CONTENT_HASH_CODES_MAP, hashCodes);
    content.set(CacheInfo.class, CacheInfo.extract(asset));
  }

  /**
   * Applies non-format specific content attributes onto passed in {@link Asset} from passed in {@link AttributesMap}
   * (usually originating from {@link Content#getAttributes()}).
   */
  public static void apply(final Asset asset, final AttributesMap content) {
    checkNotNull(asset);
    checkNotNull(content);
    final NestedAttributesMap contentAttributes = asset.attributes().child(P_CONTENT);
    contentAttributes.set(P_LAST_MODIFIED, toDate(content.get(Content.CONTENT_LAST_MODIFIED, DateTime.class)));
    contentAttributes.set(P_ETAG, content.get(Content.CONTENT_ETAG, String.class));
    final CacheInfo cacheInfo = content.get(CacheInfo.class);
    if (cacheInfo != null) {
      CacheInfo.apply(asset, cacheInfo);
    }
  }
}
