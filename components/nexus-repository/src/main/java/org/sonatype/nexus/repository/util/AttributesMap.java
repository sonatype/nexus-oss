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
package org.sonatype.nexus.repository.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Container for [String -> Object] attributes.
 *
 * @since 3.0
 */
public class AttributesMap
  extends ComponentSupport
  implements Iterable<Entry<String,Object>>
{
  protected final Map<String, Object> backing;

  public AttributesMap(final Map<String, Object> backing) {
    this.backing = checkNotNull(backing);
  }

  public AttributesMap() {
    this(Maps.<String, Object>newHashMap());
  }

  private String key(final Class type) {
    checkNotNull(type);
    return type.getName();
  }

  /**
   * Coerce value to type.
   */
  private <T> T coerce(final Object value, final Class<T> type) {
    // TODO: PropertyEditor coercion?
    log.trace("Coerce: {} -> {}", value, type);
    return type.cast(value);
  }

  // TODO: get w/default-value?

  @Nullable
  public Object get(final String key) {
    checkNotNull(key);
    return backing.get(key);
  }

  @Nullable
  public <T> T get(final Class<T> type) {
    Object value = get(key(type));
    return type.cast(value);
  }

  /**
   * Get attribute value with coercion support.
   */
  public <T> T get(final String key, final Class<T> type) {
    checkNotNull(type);
    Object value = get(key);
    return coerce(value, type);
  }

  public Object require(final String key) {
    Object value = get(key);
    checkState(value != null, "Missing: %s", key);
    return value;
  }

  public <T> T require(final Class<T> type) {
    T value = get(type);
    checkState(value != null, "Missing: %s", key(type));
    return value;
  }

  /**
   * Require attribute value with coercion support.
   */
  public <T> T require(final String key, final Class<T> type) {
    checkNotNull(type);
    Object value = require(key);
    return coerce(value, type);
  }

  @Nullable
  public Object set(final String key, final Object value) {
    checkNotNull(key);
    checkNotNull(value);
    Object replaced = backing.put(key, value);
    log.trace("Set: {}={} ({})", key, value, value.getClass().getName());
    return replaced;
  }

  @Nullable
  public <T> Object set(final Class<T> type, final T value) {
    return set(key(type), value);
  }

  @Nullable
  public Object remove(final String key) {
    checkNotNull(key);
    Object removed = backing.remove(key);
    if (removed != null) {
      log.trace("Removed: {}", key);
    }
    return removed;
  }

  @Nullable
  public Object remove(final Class type) {
    return remove(key(type));
  }

  public boolean defined(final String key) {
    checkNotNull(key);
    return backing.containsKey(key);
  }

  public boolean defined(final Class type) {
    return defined(key(type));
  }

  public Set<String> keys() {
    return backing.keySet();
  }

  public Set<Entry<String,Object>> entries() {
    return backing.entrySet();
  }

  @Override
  public Iterator<Entry<String, Object>> iterator() {
    return backing.entrySet().iterator();
  }

  public boolean isEmpty() {
    return backing.isEmpty();
  }

  public int size() {
    return backing.size();
  }

  public void clear() {
    backing.clear();
    log.trace("Cleared");
  }

  @Override
  public String toString() {
    return backing.toString();
  }
}
