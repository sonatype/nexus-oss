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

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

/**
 * Maven content.
 *
 * @since 3.0
 */
public class MavenContent
    extends Content
{
  private final Map<HashAlgorithm, HashCode> hashes;

  public MavenContent(final Payload payload,
                      final @Nullable DateTime lastModified,
                      final @Nullable String etag,
                      final @Nullable Map<HashAlgorithm, HashCode> hashes)
  {
    super(payload, lastModified, etag);
    final Map<HashAlgorithm, HashCode> hc = Maps.newLinkedHashMap();
    if (hashes != null) {
      hc.putAll(hashes);
    }
    this.hashes = ImmutableMap.copyOf(hc);
  }

  @Nonnull
  public Set<HashAlgorithm> getHashAlgorithms() {
    return hashes.keySet();
  }

  @Nonnull
  public Map<HashAlgorithm, HashCode> getHashCodes() {
    return hashes;
  }
}
