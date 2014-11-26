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
package org.sonatype.nexus.component.model;

import java.util.Map;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for {@link Entity} implementations.
 *
 * @since 3.0
 */
abstract class BaseEntity
    implements Entity
{
  private final String className;

  private final Map<String, Object> props = Maps.newHashMap();

  BaseEntity(String className) {
    this.className = checkNotNull(className);
  }

  BaseEntity(String className, Map<String, Object> props) {
    this.className = checkNotNull(className);
    this.props.putAll(checkNotNull(props));
    this.props.remove("@class");
  }

  public String getClassName() {
    return className;
  }

  public Entity put(String name, Object value) {
    props.put(name, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String name, Class<T> clazz) {
    return (T) props.get(name);
  }

  public Map<String, Object> toMap(boolean includeClass) {
    Map<String, Object> map = Maps.newHashMap(props);
    if (includeClass) {
      map.put("@class", className);
    }
    return map;
  }
}
