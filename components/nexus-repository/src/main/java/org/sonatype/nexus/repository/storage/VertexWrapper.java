/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage;

import java.util.Set;

import javax.annotation.Nullable;

import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

/**
 * Wraps an {@code OrientVertex} to provide a simpler API for working with nodes in the component metadata graph.
 *
 * @since 3.0
 */
interface VertexWrapper
{
  /**
   * Gets the underlying vertex.
   */
  OrientVertex vertex();

  /**
   * Gets the identity of the underlying record. Note that this will be a temporary id until the underlying
   * record is saved for the first time. After the first save, the id will remain permanent.
   */
  ORID id();

  /**
   * Tells whether the underlying record is new.
   */
  boolean isNew();

  /**
   * Gets all top-level property names.
   */
  Set<String> propertyNames();

  /**
   * Gets a property value as a string or {@code null} if undefined.
   */
  String get(String name);

  /**
   * Gets a property value coerced to the given type or {@code null} if undefined.
   */
  @Nullable
  <T> T get(String name, Class<T> type);

  /**
   * Gets a property value as a string or throws a runtime exception if undefined.
   */
  String require(String name);

  /**
   * Gets a property value coerced to the given type or throws a runtime exception if undefined.
   */
  <T> T require(String name, Class<T> type);

  /**
   * Sets a property to a given value, or {@code null} to remove it.
   */
  void set(String name, @Nullable Object value);
}
