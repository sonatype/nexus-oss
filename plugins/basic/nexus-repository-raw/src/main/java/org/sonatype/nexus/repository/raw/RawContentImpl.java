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
package org.sonatype.nexus.repository.raw;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.repository.view.Payload;

import com.google.common.io.ByteStreams;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class RawContentImpl
    implements RawContent
{
  private final String type;

  private final byte[] bytes;

  private final DateTime lastModified;

  public RawContentImpl(final Payload payload) throws IOException {
    checkNotNull(payload);
    this.type = payload.getContentType();
    try (InputStream input = checkNotNull(payload.openInputStream())) {
      this.bytes = ByteStreams.toByteArray(input);
    }
    this.lastModified = payload.getLastModified();
  }

  @Override
  public String getContentType() {
    return type;
  }

  @Override
  public long getSize() {
    return bytes.length;
  }

  @Override
  public DateTime getLastModified() {
    return lastModified;
  }

  @Override
  public InputStream openInputStream() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        ", size=" + bytes.length +
        ", lastModified=" + lastModified +
        '}';
  }
}
