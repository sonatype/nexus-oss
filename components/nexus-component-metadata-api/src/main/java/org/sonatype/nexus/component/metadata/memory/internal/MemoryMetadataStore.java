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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.metadata.api.AbstractMetadataStore;
import org.sonatype.nexus.component.metadata.api.MetadataHandle;
import org.sonatype.nexus.component.metadata.api.MetadataIndex;
import org.sonatype.nexus.component.metadata.api.MetadataKeyIterable;
import org.sonatype.nexus.component.metadata.api.MetadataTypeRegistry;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.apache.commons.lang.StringUtils;

public class MemoryMetadataStore<T>
    extends AbstractMetadataStore<T>
{
  private final Map<String, MetadataHandle<T>> map = new ConcurrentHashMap<>();

  private final Set<UniqueEnforcer> uniqueEnforcers = new HashSet<>();

  private long sequence;

  public MemoryMetadataStore(MetadataTypeRegistry registry, Class<T> clazz) {
    super(registry, clazz);
    for (MetadataIndex index: type.getIndexes()) {
      if (index.isUnique()) {
        uniqueEnforcers.add(new UniqueEnforcer(index.getColumns()));
      }
    }
  }

  @Override
  public MetadataHandle<T> create(final T metadata) {
    String key = generateKey();
    MetadataHandle<T> handle = new MetadataHandle<>(key, generateVersion(), metadata);
    synchronized (map) {
      for (UniqueEnforcer enforcer: uniqueEnforcers) {
        enforcer.add(metadata);
      }
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
      MetadataHandle<T> handle = getHandleOrNull(key, version);
      if (handle == null) {
        return null;
      }
      MetadataHandle<T> newHandle = new MetadataHandle<>(key, generateVersion(), metadata);
      for (UniqueEnforcer enforcer: uniqueEnforcers) {
        enforcer.delete(handle.getMetadata());
        enforcer.add(metadata);
      }
      map.put(key, newHandle);
      return newHandle;
    }
  }

  @Override
  public boolean delete(final String key, @Nullable final String version) {
    synchronized (map) {
      MetadataHandle<T> handle = getHandleOrNull(key, version);
      if (handle == null) {
        return false;
      }
      map.remove(key);
      for (UniqueEnforcer enforcer: uniqueEnforcers) {
        enforcer.delete(handle.getMetadata());
      }
      return true;
    }
  }

  @Nullable
  private MetadataHandle<T> getHandleOrNull(String key, @Nullable String version) {
    MetadataHandle<T> handle = retrieve(key);
    if (handle == null) {
      return null;
    }
    if (version != null && !handle.getVersion().equals(version)) {
      return null;
    }
    return handle;
  }

  @Override
  protected MetadataKeyIterable find(final Map<String, Object> filter, final MetadataIndex index) {
    return new MetadataKeyIterable() {
      @Override
      public void close() throws Exception {
        // nothing to do
      }

      @Override
      public Iterator<String> iterator() {
        return Iterators.filter(map.keySet().iterator(), new Predicate<String>() {
          @Override
          public boolean apply(@Nullable final String key) {
            return matches(map.get(key).getMetadata(), filter);
          }
        });
      }
    };
  }

  private boolean matches(T metadata, Map<String, Object> filter) {
    for (String attribute: filter.keySet()) {
      Object value = invokeGetter(metadata, attribute);
      if (value == null || !value.equals(filter.get(attribute))) {
        return false;
      }
    }
    return true;
  }

  private synchronized String generateKey() {
    return type.getName() + ":" + sequence++;
  }

  private synchronized String generateVersion() {
    return type.getName() + ":version:" + sequence++;
  }

  private static Object invokeGetter(Object metadata, String attribute) {
    String suffix = StringUtils.capitalize(attribute);
    Method method = null;
    try {
      method = metadata.getClass().getMethod("get" + suffix);
    }
    catch (NoSuchMethodException e) {
      try {
        method = metadata.getClass().getMethod("is" + suffix);
      }
      catch (NoSuchMethodException e2) {
        throw new IllegalArgumentException("No getter defined for attribute " + attribute +
            " in class " + metadata.getClass());
      }
    }
    try {
      return method.invoke(metadata);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static class UniqueEnforcer
  {
    private final List<String> attributes;

    private final Set<String> values = new HashSet<>();

    public UniqueEnforcer(List<String> attributes) {
      this.attributes = attributes;
    }

    public void add(Object metadata) {
      String value = getAttributesAsString(metadata);
      synchronized (values) {
        if (values.contains(value)) {
          throw new IllegalArgumentException("Unique constraint violated");
        }
        values.add(value);
      }
    }

    public void delete(Object metadata) {
      String value = getAttributesAsString(metadata);
      synchronized (values) {
        values.remove(value);
      }
    }

    private String getAttributesAsString(Object metadata) {
      StringBuilder builder = new StringBuilder(metadata.getClass().getSimpleName());
      for (String attribute: attributes) {
        builder.append("\n");
        builder.append(attribute);
        builder.append("=");
        builder.append(invokeGetter(metadata, attribute));
      }
      return builder.toString();
    }
  }
}
