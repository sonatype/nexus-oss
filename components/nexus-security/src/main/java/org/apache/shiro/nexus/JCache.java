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

public class JCache<K, V>
  implements Cache<K, V>
{
  private final javax.cache.Cache<K, V> cache;

  public JCache(final javax.cache.Cache<K, V> cache) { this.cache = cache; }

  /**
   * Returns the Cached value stored under the specified {@code key} or
   * {@code null} if there is no Cache entry for that {@code key}.
   *
   * @param key the key that the value was previous added with
   * @return the cached object or {@code null} if there is no entry for the specified {@code key}
   * @throws CacheException if there is a problem accessing the underlying cache system
   */
  @Override
  public V get(final K key) throws CacheException {
    try {
      return cache.get(key);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  /**
   * Adds a Cache entry.
   *
   * @param key   the key used to identify the object being stored.
   * @param value the value to be stored in the cache.
   * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
   * @throws CacheException if there is a problem accessing the underlying cache system
   */
  @Override
  public V put(final K key, final V value) throws CacheException {
    try {
      return cache.getAndPut(key, value);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  /**
   * Remove the cache entry corresponding to the specified key.
   *
   * @param key the key of the entry to be removed.
   * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
   * @throws CacheException if there is a problem accessing the underlying cache system
   */
  @Override
  public V remove(final K key) throws CacheException {
    try {
      return cache.getAndRemove(key);
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  /**
   * Clear all entries from the cache.
   *
   * @throws CacheException if there is a problem accessing the underlying cache system
   */
  @Override
  public void clear() throws CacheException {
    try {
      cache.clear();
    }
    catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  /**
   * Returns the number of entries in the cache.
   *
   * @return the number of entries in the cache.
   */
  @Override
  public int size() {
    int size = 0;
    for (final Entry<K, V> ignored : cache) {
      size++;
    }
    return size;
  }

  /**
   * Returns a view of all the keys for entries contained in this cache.
   *
   * @return a view of all the keys for entries contained in this cache.
   */
  @Override
  public Set<K> keys() {
    Set<K> keys = new HashSet<>();
    for (final Entry<K, V> entry : cache) {
      keys.add(entry.getKey());
    }
    return Collections.unmodifiableSet(keys);
  }

  /**
   * Returns a view of all of the values contained in this cache.
   *
   * @return a view of all of the values contained in this cache.
   */
  @Override
  public Collection<V> values() {
    Collection<V> values = new ArrayList<>();
    for (final Entry<K, V> entry : cache) {
      values.add(entry.getValue());
    }
    return Collections.unmodifiableCollection(values);
  }
}
