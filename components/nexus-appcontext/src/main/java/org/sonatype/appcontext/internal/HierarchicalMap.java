/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.appcontext.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Map (based on HashMap, so all of it's peculiarities applies), that might have a parent. Parent is referred only in
 * case of query operations ({@link #get(Object)}, {@link #containsKey(Object)}, {@link #containsValue(Object)}), so
 * for
 * example the {@link #get(Object)} method may "bubble" up on multiple ancestors to grab a value. Still, even if
 * current
 * map has no entries, but it serves entries from it's parent, {@link #size()} of this map will return 0.
 *
 * @author cstamas
 */
public class HierarchicalMap<K, V>
    extends ConcurrentHashMap<K, V>
    implements Map<K, V>
{
  private static final long serialVersionUID = 3445870461584217031L;

  private final Map<K, V> parent;

  public HierarchicalMap() {
    this(null);
  }

  public HierarchicalMap(final Map<K, V> parent) {
    super();
    this.parent = checkParentContext(parent);
  }

  public Map<K, V> getParent() {
    return parent;
  }

  protected Map<K, V> checkParentContext(final Map<K, V> context)
      throws IllegalArgumentException
  {
    if (context != null) {
      if (this == context) {
        throw new IllegalArgumentException(
            "The context cannot be parent of itself! The parent instance cannot equals to this instance!");
      }

      if (context instanceof HierarchicalMap) {
        Map<K, V> otherParentContext = ((HierarchicalMap<K, V>) context).getParent();
        while (otherParentContext != null) {
          if (this == otherParentContext) {
            throw new IllegalArgumentException(
                "The context cannot be an ancestor of itself! Cycle detected!");
          }

          if (otherParentContext instanceof HierarchicalMap) {
            otherParentContext = ((HierarchicalMap<K, V>) otherParentContext).getParent();
          }
          else {
            otherParentContext = null;
          }
        }
      }
    }
    return context;
  }

  // ==

  @Override
  public boolean containsKey(Object key) {
    return containsKey(key, true);
  }

  public boolean containsKey(Object key, boolean fallBackToParent) {
    boolean result = super.containsKey(key);
    if (fallBackToParent && !result && getParent() != null) {
      result = getParent().containsKey(key);
    }
    return result;
  }

  @Override
  public boolean containsValue(Object val) {
    return containsValue(val, true);
  }

  public boolean containsValue(Object val, boolean fallBackToParent) {
    boolean result = super.containsValue(val);
    if (fallBackToParent && !result && getParent() != null) {
      result = getParent().containsValue(val);
    }
    return result;
  }

  @Override
  public V get(Object key) {
    return get(key, true);
  }

  public V get(Object key, boolean fallBackToParent) {
    if (containsKey(key, false)) {
      return super.get(key);
    }
    else if (fallBackToParent && getParent() != null) {
      return getParent().get(key);
    }
    else {
      return null;
    }
  }

  @Override
  public Set<K> keySet() {
    final Set<K> result = new HashSet<K>();
    if (getParent() != null) {
      result.addAll(getParent().keySet());
    }
    result.addAll(super.keySet());
    return Collections.unmodifiableSet(result);
  }

  @Override
  public Collection<V> values() {
    final ArrayList<V> result = new ArrayList<V>();
    if (getParent() != null) {
      result.addAll(getParent().values());
    }
    result.addAll(super.values());
    return Collections.unmodifiableCollection(result);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    final Set<Entry<K, V>> result = new HashSet<Entry<K, V>>();
    if (getParent() != null) {
      final Set<Entry<K, V>> parentEntries = getParent().entrySet();
      for (Entry<K, V> parentEntry : parentEntries) {
        if (!containsKey(parentEntry.getKey(), false)) {
          result.add(parentEntry);
        }
      }
    }
    result.addAll(super.entrySet());
    return Collections.unmodifiableSet(result);
  }

  @Override
  public boolean isEmpty() {
    return keySet().isEmpty();
  }

  @Override
  public int size() {
    return keySet().size();
  }

  // ==

  /**
   * "Pulls out" this map from the hierarchy. It simply returns a map with content that is to be found in this
   * instance. The returned map is a copy, so changing it does not affect this instance!
   *
   * @return a map that contains elements from this instance.
   */
  public Map<K, V> pullOut() {
    final HashMap<K, V> result = new HashMap<K, V>();
    for (Entry<K, V> entry : super.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Flattens this instance of {@link HierarchicalMap} into single map. It simply calculates which entries are
   * "visible" and returns a plain map. The returned map is a copy, so changing it does not affect this instance.
   *
   * @return a map that contains elements from this instance including the hierarchy (if any).
   */
  public Map<K, V> flatten() {
    final HashMap<K, V> result = new HashMap<K, V>();
    final Map<K, V> parent = getParent();
    if (parent != null) {
      if (parent instanceof HierarchicalMap) {
        result.putAll(((HierarchicalMap<K, V>) parent).flatten());
      }
      else {
        result.putAll(parent);
      }
    }
    result.putAll(this);
    return result;
  }
}