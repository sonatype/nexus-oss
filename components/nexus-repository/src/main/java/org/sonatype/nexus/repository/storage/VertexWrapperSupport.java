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
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Supporting base class for {@link VertexWrapper} implementations.
 *
 * @since 3.0
 */
abstract class VertexWrapperSupport
    implements VertexWrapper
{
  protected final OrientVertex vertex;

  VertexWrapperSupport(OrientVertex vertex) {
    this.vertex = checkNotNull(vertex);
  }

  @Override
  public OrientVertex vertex() {
    return vertex;
  }

  @Override
  public ORID id() {
    return vertex.getRecord().getIdentity();
  }

  @Override
  public boolean isNew() {
    return id().isNew();
  }

  @Override
  public Set<String> propertyNames() {
    return vertex.getPropertyKeys();
  }

  @Override
  public String get(final String name) {
    return get(name, String.class);
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(final String name, final Class<T> type) {
    checkNotNull(name);
    checkNotNull(type);

    Object value = vertex.getProperty(name);
    if (value != null) {
      // TODO: PropertyEditor coercion? (see also, AttributesMap#coerce())
      if (type == DateTime.class) {
        return (T) new DateTime(value);
      }
      return (T) type.cast(value);
    }
    return null;
  }

  @Override
  public String require(final String name) {
    return require(name, String.class);
  }

  @Override
  public <T> T require(final String name, final Class<T> type) {
    T value = get(name, type);
    checkState(value != null, "Missing property: %s", name);
    return value;
  }

  @Override
  public void set(final String name, final @Nullable Object value) {
    checkNotNull(name);
    if (value == null) {
      vertex.removeProperty(name);
    }
    else if (value instanceof DateTime) {
      vertex.setProperty(name, ((DateTime) value).toDate());
    }
    else {
      vertex.setProperty(name, value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof VertexWrapperSupport)) {
      return false;
    }
    return vertex.equals(((VertexWrapper) o).vertex());
  }

  @Override
  public int hashCode() {
    return vertex().hashCode();
  }
}
