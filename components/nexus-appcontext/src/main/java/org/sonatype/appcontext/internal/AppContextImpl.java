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
import java.util.Map;
import java.util.Set;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.AppContextInterpolationException;
import org.sonatype.appcontext.lifecycle.AppContextLifecycleManager;

import org.codehaus.plexus.interpolation.InterpolationException;

public class AppContextImpl
    implements AppContext
{
  private final long created;

  private final String id;

  private final AppContext parent;

  private final HierarchicalMap<String, AppContextEntry> hmap;

  private final AppContextLifecycleManager lifecycleManager;

  private volatile long modified;

  private volatile int parentGeneration;

  private volatile int generation;

  public AppContextImpl(final long created, final String id, final AppContextImpl parent,
                        final Map<String, AppContextEntry> sourcedEntries)
  {
    this.created = created;
    this.modified = this.created;
    this.generation = 0;
    this.id = Preconditions.checkNotNull(id);

    this.parent = parent;

    if (parent != null) {
      this.parentGeneration = parent.getGeneration();
      this.hmap = new HierarchicalMap<String, AppContextEntry>(parent.getEntries());
    }
    else {
      this.parentGeneration = -1;
      this.hmap = new HierarchicalMap<String, AppContextEntry>();
    }

    this.hmap.putAll(sourcedEntries);
    this.lifecycleManager = new AppContextLifecycleManagerImpl();
  }

  public long getCreated() {
    return created;
  }

  public long getModified() {
    getGeneration(); // this will trigger setting of modified and generation if needed
    return modified;
  }

  public synchronized int getGeneration() {
    if (getParent() != null && getParent().getGeneration() > parentGeneration) {
      parentGeneration = getParent().getGeneration();
      markContextModified(getParent().getModified());
    }
    return generation;
  }

  public String getId() {
    return id;
  }

  public AppContext getParent() {
    return parent;
  }

  public AppContextLifecycleManager getLifecycleManager() {
    return lifecycleManager;
  }

  public AppContextEntry getAppContextEntry(String key) {
    return hmap.get(key);
  }

  public Map<String, Object> flatten() {
    final Map<String, AppContextEntry> flattenEntries = flattenAppContextEntries();
    final HashMap<String, Object> result = new HashMap<String, Object>(flattenEntries.size());
    for (AppContextEntry entry : flattenEntries.values()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return Collections.unmodifiableMap(result);
  }

  public Map<String, AppContextEntry> flattenAppContextEntries() {
    final HashMap<String, AppContextEntry> result = new HashMap<String, AppContextEntry>();
    result.putAll(hmap.flatten());
    return Collections.unmodifiableMap(result);
  }

  public String interpolate(final String string)
      throws AppContextInterpolationException
  {
    try {
      return InternalFactory.getInterpolator(this).interpolate(string);
    }
    catch (InterpolationException e) {
      throw new AppContextInterpolationException(e.getMessage(), e);
    }
  }

  // ==

  protected HierarchicalMap<String, AppContextEntry> getEntries() {
    return hmap;
  }

  protected void markContextModified() {
    markContextModified(System.currentTimeMillis());
  }

  protected synchronized void markContextModified(final long timestamp) {
    generation++;
    modified = Math.max(modified, timestamp);
  }

  // ==

  public int size() {
    return hmap.size();
  }

  public boolean isEmpty() {
    return hmap.isEmpty();
  }

  public boolean containsKey(Object key) {
    return hmap.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return hmap.containsValue(value);
  }

  public Object get(Object key) {
    final AppContextEntry entry = hmap.get(key);
    if (entry != null) {
      return entry.getValue();
    }
    else {
      return null;
    }
  }

  public Object put(String key, Object value) {
    final AppContextEntry oldEntry =
        hmap.put(Preconditions.checkNotNull(key), new AppContextEntryImpl(created, key, value, value,
            new ProgrammaticallySetSourceMarker()));
    markContextModified();
    if (oldEntry != null) {
      return oldEntry.getValue();
    }
    else {
      return null;
    }
  }

  public Object remove(Object key) {
    final AppContextEntry oldEntry = hmap.remove(key);
    markContextModified();
    if (oldEntry != null) {
      return oldEntry.getValue();
    }
    else {
      return null;
    }
  }

  public void putAll(Map<? extends String, ? extends Object> m) {
    for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  public void clear() {
    hmap.clear();
    markContextModified();
  }

  public Set<String> keySet() {
    return Collections.unmodifiableSet(hmap.keySet());
  }

  public Collection<Object> values() {
    final ArrayList<Object> result = new ArrayList<Object>(hmap.size());
    for (Map.Entry<String, AppContextEntry> entry : hmap.entrySet()) {
      result.add(entry.getValue().getValue());
    }
    return Collections.unmodifiableList(result);
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    final Map<String, Object> result = new HashMap<String, Object>(hmap.size());
    for (Map.Entry<String, AppContextEntry> entry : hmap.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getValue());
    }
    return Collections.unmodifiableSet(result.entrySet());
  }

  // ==

  public String toString() {
    return hmap.values().toString();
  }
}
