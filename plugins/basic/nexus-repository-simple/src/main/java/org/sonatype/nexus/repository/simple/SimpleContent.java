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
package org.sonatype.nexus.repository.simple;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.hash.MultiHashingInputStream;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA512;

/**
 * Simple content.
 *
 * @since 3.0
 */
public class SimpleContent
{
  private final static List<HashAlgorithm> hashAlgorithms = Lists.newArrayList(MD5, SHA1, SHA512);

  @Nullable
  private final String type;

  private final byte[] bytes;

  private final Map<HashAlgorithm, HashCode> hashes;

  public SimpleContent(final Payload payload) throws IOException {
    checkNotNull(payload);
    this.type = payload.getContentType();
    try (MultiHashingInputStream input = new MultiHashingInputStream(hashAlgorithms,
        checkNotNull(payload.openInputStream()))) {
      this.bytes = ByteStreams.toByteArray(input);
      this.hashes = input.hashes();
    }
  }

  @Nullable
  public String getType() {
    return type;
  }

  public long getSize() {
    return bytes.length;
  }

  public Map<HashAlgorithm, HashCode> getHashes() {
    return hashes;
  }

  public Payload toPayload() {
    return new BytesPayload(bytes, type);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        ", size=" + bytes.length +
        ", hashes=" + hashes +
        '}';
  }
}
