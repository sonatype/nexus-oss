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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Container for [String -> Object] attributes.
 *
 * @since 3.0
 */
public class AttributesContainer
  extends ComponentSupport
  implements Iterable<Entry<String,Object>>
{
  private final Map<String, Object> backing;

  public AttributesContainer(final Map<String, Object> backing) {
    this.backing = checkNotNull(backing);
  }

  @Nullable
  public Object get(final String key) {
    checkNotNull(key);
    return backing.get(key);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(final Class<T> type) {
    checkNotNull(type);
    return (T) get(type.getName());
  }

  public Object require(final String key) {
    Object value = get(key);
    checkState(value != null, "Missing attribute: %s", key);
    return value;
  }

  public <T> T require(final Class<T> type) {
    T value = get(type);
    checkState(value != null, "Missing attribute: %s", type.getName());
    return value;
  }

  public Object set(final String key, final Object value) {
    checkNotNull(key);
    checkNotNull(value);
    log.trace("Set: {}={}", key, value);
    return backing.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T set(final Class<T> type, final T value) {
    checkNotNull(type);
    return (T) set(type.getName(), value);
  }

  public void remove(final String key) {
    checkNotNull(key);
    log.trace("Remove: {}", key);
    backing.remove(key);
  }

  public void remove(final Class type) {
    checkNotNull(type);
    remove(type.getName());
  }

  public boolean defined(final String key) {
    checkNotNull(key);
    return backing.containsKey(key);
  }

  public boolean defined(final Class type) {
    checkNotNull(type);
    return defined(type.getName());
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
    log.debug("Cleared");
  }

  @Override
  public String toString() {
    return backing.toString();
  }

  // TODO: Coercion support
}
