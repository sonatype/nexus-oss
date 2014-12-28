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
package org.sonatype.nexus.repository.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link Configuration} attributes.
 *
 * @since 3.0
 */
public class ConfigurationAttributes
{
  @VisibleForTesting
  static final String GRANDPARENT_SEPARATOR = "::";

  @Nullable
  private final ConfigurationAttributes parent;

  private final String key;

  private final Map<String,Object> map;

  public ConfigurationAttributes(final String key, final Map<String, Object> map) {
    this(null, key, map);
  }

  @VisibleForTesting
  ConfigurationAttributes(final @Nullable ConfigurationAttributes parent, final String key,
                          final Map<String, Object> map) {
    this.parent = parent;
    this.key = checkNotNull(key);
    this.map = checkNotNull(map);
  }

  @Nullable
  public ConfigurationAttributes getParent() {
    return parent;
  }

  @VisibleForTesting
  String getParentKey() {
    if (parent != null) {
      // fully-qualify parent key if it has a grandparent
      if (parent.parent != null) {
        return parent.getParentKey() + GRANDPARENT_SEPARATOR + parent.getKey();
      }
      return parent.getKey();
    }
    return null;
  }

  public String getKey() {
    return key;
  }

  public boolean defined(final String name) {
    checkNotNull(name);
    return map.containsKey(name);
  }

  @Nullable
  public Object get(final String name) {
    checkNotNull(name);
    return map.get(name);
  }

  public void set(final String name, final Object value) {
    checkNotNull(name);
    checkNotNull(value);
    checkState(!(value instanceof Map), "Use child() to set a map value");
    map.put(name, value);
  }

  public void remove(final String name) {
    checkNotNull(name);
    map.remove(name);
  }

  public ConfigurationAttributes child(final String name) {
    checkNotNull(name);

    Object child = map.get(name);
    if (child == null) {
      child = new HashMap<String,Object>();
      map.put(name, child);
    }
    else {
      checkState(child instanceof Map, "child '%s' not a Map", name);
    }
    //noinspection unchecked,ConstantConditions
    return new ConfigurationAttributes(this, name, (Map<String, Object>) child);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "parent=" + getParentKey() +
        ", key='" + key + '\'' +
        ", map=" + map +
        '}';
  }

  //
  // Coercion support
  //

  //public <T> T get(final String name, final Class<T> type) {
  //  checkNotNull(name);
  //  checkNotNull(type);
  //
  //  // TODO:
  //  return null;
  //}

  //public <T> void set(final String name, final Class<T> type, final T value) {
  //  checkNotNull(name);
  //  checkNotNull(type);
  //  checkNotNull(value);
  //
  //  // TODO:
  //}
}
