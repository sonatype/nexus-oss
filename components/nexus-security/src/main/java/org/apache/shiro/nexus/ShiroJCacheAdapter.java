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
package org.apache.shiro.nexus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache.Entry;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shiro {@link javax.cache.Cache} to {@link Cache} adapter.
 *
 * @since 3.0
 */
public class ShiroJCacheAdapter<K, V>
  implements Cache<K, V>
{
  private final javax.cache.Cache<K, V> cache;

  public ShiroJCacheAdapter(final javax.cache.Cache<K, V> cache) {
    this.cache = checkNotNull(cache);
  }

  @Override
  public V get(final K key) throws CacheException {
    try {
      return cache.get(key);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public V put(final K key, final V value) throws CacheException {
    try {
      return cache.getAndPut(key, value);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public V remove(final K key) throws CacheException {
    try {
      return cache.getAndRemove(key);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public void clear() throws CacheException {
    try {
      cache.clear();
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public int size() {
    int size = 0;
    for (final Entry<K, V> ignored : cache) {
      size++;
    }
    return size;
  }

  @Override
  public Set<K> keys() {
    Set<K> keys = new HashSet<>();
    for (final Entry<K, V> entry : cache) {
      keys.add(entry.getKey());
    }
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public Collection<V> values() {
    Collection<V> values = new ArrayList<>();
    for (final Entry<K, V> entry : cache) {
      values.add(entry.getValue());
    }
    return Collections.unmodifiableCollection(values);
  }
}
