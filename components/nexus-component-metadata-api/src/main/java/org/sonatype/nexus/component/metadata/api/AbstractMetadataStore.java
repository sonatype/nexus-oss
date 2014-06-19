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
package org.sonatype.nexus.component.metadata.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractMetadataStore<T>
    implements MetadataStore<T>
{
  protected final MetadataType type;

  protected final Class<T> clazz;

  protected AbstractMetadataStore(MetadataType type, Class<T> clazz) {
    this.type = checkNotNull(type);
    this.clazz = checkNotNull(clazz);
  }

  @Override
  public MetadataKeyIterable find(final MetadataBuilder<T> filter, final MetadataIndex index) {
    checkNotNull(filter);
    checkNotNull(index);
    return find(toMap(filter), index);
  }

  protected abstract MetadataKeyIterable find(Map<String, Object> filter, MetadataIndex index);

  private Map<String, Object> toMap(MetadataBuilder<T> filter) {
    Map<String, Object> map = new HashMap<>();
    for (MetadataType.Attribute attribute: type.getAttributes().values()) {
      try {
        Field field = filter.getClass().getDeclaredField(attribute.getName());
        Object value = field.get(filter);
        if (value != null) {
          map.put(attribute.getName(), value);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (NoSuchFieldException e) {
        throw new IllegalArgumentException("No field in builder for metadata attribute: " + attribute.getName());
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
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
