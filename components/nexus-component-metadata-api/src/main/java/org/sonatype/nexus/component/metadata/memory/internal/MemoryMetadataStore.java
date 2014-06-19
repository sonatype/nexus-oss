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
package org.sonatype.nexus.component.metadata.memory.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.metadata.api.AbstractMetadataStore;
import org.sonatype.nexus.component.metadata.api.MetadataHandle;
import org.sonatype.nexus.component.metadata.api.MetadataIndex;
import org.sonatype.nexus.component.metadata.api.MetadataKeyIterator;
import org.sonatype.nexus.component.metadata.api.MetadataType;

public class MemoryMetadataStore<T>
    extends AbstractMetadataStore<T>
{
  private final Map<String, MetadataHandle<T>> map = new ConcurrentHashMap<>();

  private final Map<Object[], MetadataHandle<T>> uniqueIndexMap = new ConcurrentHashMap<>();

  // if a unique index exists, this will be defined so uniqueIndexMap can be maintained
  private List<String> uniqueIndexColumns;

  private long sequence;

  public MemoryMetadataStore(MetadataType type, Class<T> clazz) {
    super(type, clazz);
    for (MetadataIndex index: type.getIndexes()) {
      if (index.isUnique()) {
        uniqueIndexColumns = index.getColumns();
      }
    }
  }

  @Override
  public MetadataHandle<T> create(final T metadata) {
    String key = generateKey();
    MetadataHandle<T> handle = new MetadataHandle<>(key, generateVersion(), metadata);
    synchronized (map) {
      map.put(key, handle);
    }
    return handle;
  }

  @Nullable
  @Override
  public MetadataHandle<T> retrieve(final String key) {
    return map.get(key);
  }

  @Override
  public MetadataHandle<T> update(final String key, @Nullable final String version, final T metadata) {
    synchronized (map) {
      MetadataHandle<T> handle = retrieve(key);
      if (handle == null) {
        throw new IllegalStateException("Key not found in store: " + key);
      }
      if (version != null && !handle.getVersion().equals(version)) {
        return null;
      }
      MetadataHandle<T> newHandle = new MetadataHandle<>(key, generateVersion(), metadata);
      map.put(key, newHandle);
      return newHandle;
    }
  }

  @Override
  public boolean delete(final String key, @Nullable final String version) {
    // TODO: Impl
    return false;
  }

  @Override
  protected MetadataKeyIterator find(final Map<String, Object> filter, final String index) {
    // TODO: Impl
    return null;
  }

  private synchronized String generateKey() {
    return type.getName() + ":" + sequence++;
  }

  private synchronized String generateVersion() {
    return type.getName() + ":version:" + sequence++;
  }
}
