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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.view.Payload;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven payload.
 *
 * @since 3.0
 */
public class MavenPayload
    implements Payload
{
  private final Payload payload;

  private final DateTime lastModified;

  private final Map<HashAlgorithm, HashCode> hashes;

  public MavenPayload(final Payload payload,
                      final @Nullable DateTime lastModified,
                      final @Nullable Map<HashAlgorithm, HashCode> hashes)
  {
    this.payload = checkNotNull(payload);
    this.lastModified = lastModified;
    final Map<HashAlgorithm, HashCode> hc = Maps.newLinkedHashMap();
    if (hashes != null) {
      hc.putAll(hashes);
    }
    this.hashes = ImmutableMap.copyOf(hc);
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return payload.openInputStream();
  }

  @Override
  public long getSize() {
    return payload.getSize();
  }

  @Nullable
  @Override
  public String getContentType() {
    return payload.getContentType();
  }

  @Nullable
  public DateTime getLastModified() {
    return lastModified;
  }

  @Nonnull
  public Set<HashAlgorithm> getHashAlgorithms() {
    return hashes.keySet();
  }

  @Nonnull
  public Map<HashAlgorithm, HashCode> getHashCodes() {
    return hashes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "payload=" + payload +
        ", lastModified='" + lastModified + '\'' +
        ", hashes='" + hashes + '\'' +
        '}';
  }
}
