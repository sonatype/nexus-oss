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
package org.sonatype.nexus.componentmetadata.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractMetadataStore<T>
    implements MetadataStore<T>
{
  protected final MetadataType<T> type;

  protected final Class<T> clazz;

  protected final MetadataTypeRegistry registry;

  protected AbstractMetadataStore(MetadataTypeRegistry registry, Class<T> clazz) {
    this.registry = checkNotNull(registry);
    this.type = registry.getType(clazz);
    this.clazz = checkNotNull(clazz);
  }

  @Override
  public MetadataKeyIterable find(final MetadataBuilder<T> filter, final MetadataIndex index) {
    checkNotNull(filter);
    checkIndex(index);
    Map<String, Object> filterAsMap = toMap(filter);
    checkFilterAsMap(filterAsMap, index);
    return find(filterAsMap, index);
  }

  private static void checkFilterAsMap(Map<String, Object> map, MetadataIndex index) {
    // all constrained attributes in the map must be part of the index
    for (String attribute: map.keySet()) {
      if (!index.getColumns().contains(attribute)) {
        throw new IllegalArgumentException("Constrained attribute '" + attribute +
            "' is not part of the specified index");
      }
    }
    // the first column of the index must be constrained by the map
    String firstColumn = index.getColumns().get(0);
    if (!map.containsKey(firstColumn)) {
      throw new IllegalArgumentException("First column, '" + firstColumn + "' of specified " +
          "index must be a constrained attribute of the query");
    }
    // if index is unique, all columns must be constrained by the query
    if (index.isUnique()) {
      for (String column: index.getColumns()) {
        if (!map.containsKey(column)) {
          throw new IllegalArgumentException("For unique index queries, all constituent " +
              "columns must be constrained attributes of the query");
        }
      }
    }
  }

  // index must be registered for this type
  private void checkIndex(MetadataIndex index) {
    checkNotNull(index);
    if (!type.getIndexes().contains(index)) {
      throw new IllegalArgumentException("Index " + index + " not registered for type " + type);
    }
  }

  protected abstract MetadataKeyIterable find(Map<String, Object> filterAsMap, MetadataIndex index);

  private Map<String, Object> toMap(MetadataBuilder<T> filter) {
    Map<String, Object> map = new HashMap<>();
    for (MetadataType.Attribute attribute: type.getAttributes().values()) {
      try {
        Field field = filter.getClass().getDeclaredField(attribute.getName());
        field.setAccessible(true);
        Object value = field.get(filter);
        if (value != null) {
          map.put(attribute.getName(), value);
        }
      } catch (Throwable t) {
        Throwables.propagate(t);
      }
    }
    return map;
  }

  @Override
  public String findFirst(final MetadataBuilder<T> filter, final MetadataIndex index) {
    checkNotNull(filter);
    checkNotNull(index);
    try (MetadataKeyIterable result = find(filter, index)) {
      Iterator<String> keys = result.iterator();
      if (keys.hasNext()) {
        return keys.next();
      }
    } catch (Throwable t) {
      Throwables.propagate(t);
    }
    return null;
  }
}
