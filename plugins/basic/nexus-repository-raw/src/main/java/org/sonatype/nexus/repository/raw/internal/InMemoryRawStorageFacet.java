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
package org.sonatype.nexus.repository.raw.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

/**
 * TODO: A temporary, in-memory {@link RawStorageFacet} until the {@link StorageFacet} changes are worked out.
 *
 * @since 3.0
 */
@Facet.Exposed
public class InMemoryRawStorageFacet
    extends FacetSupport
    implements RawStorageFacet
{
  private final Map<String, RawContent> inMemory = Maps.newConcurrentMap();

  @Nullable
  @Override
  public RawContent get(final String path) throws IOException {
    return inMemory.get(path);
  }

  @Override
  public void put(final String path, final RawContent content) throws IOException {
    try (InputStream inputStream = content.openInputStream()) {

      final RawContent byteContent = toByteArrayRawContent(content, inputStream);
      inMemory.put(path, byteContent);
    }
  }

  @Override
  public boolean delete(final String path) throws IOException {
    return inMemory.remove(path) != null;
  }

  /**
   * @return a RawContent impl based on a byte array, which can live stably in memory
   */
  private RawContent toByteArrayRawContent(final RawContent content, final InputStream inputStream) throws IOException
  {
    final String contentType = content.getContentType();
    final long size = content.getSize();


    final byte[] bytes = ByteStreams.toByteArray(inputStream);

    return new RawContent()
    {
      @Override
      public String getContentType() {
        return contentType;
      }

      @Override
      public long getSize() {
        return size;
      }

      @Override
      public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
      }
    };
  }
}
