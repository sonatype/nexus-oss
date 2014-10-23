/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.services.hash;

import java.io.IOException;

import org.sonatype.nexus.component.model.Asset;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hashes {@link Asset} content using common digest algorithms.
 * 
 * @since 3.0
 */
public enum AssetHasher
{
  MD5(Hashing.md5()), SHA1(Hashing.sha1()), SHA512(Hashing.sha512());

  private final HashFunction function;

  private AssetHasher(final HashFunction function) {
    this.function = checkNotNull(function);
  }

  /**
   * @param asset The asset to hash
   * @return Hash as a series of bytes
   * @throws IOException
   */
  public byte[] hash(final Asset asset) throws IOException {
    try (final HashingInputStream is = new HashingInputStream(function, checkNotNull(asset).openStream())) {
      ByteStreams.copy(is, ByteStreams.nullOutputStream());
      return is.hash().asBytes();
    }
  }
}
