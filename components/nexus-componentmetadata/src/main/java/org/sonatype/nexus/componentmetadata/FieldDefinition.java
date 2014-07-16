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
package org.sonatype.nexus.componentmetadata;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines the name, type, and attributes of a field.
 *
 * @since 3.0
 */
public class FieldDefinition
{
  private final String name;

  private final Class type;

  private boolean notNull;

  private boolean indexed;

  private boolean unique;

  /**
   * Creates a minimally valid field definition.
   *
   * @param name the name of the field.
   * @param type the Java class of the field.
   */
  public FieldDefinition(String name, Class type) {
    this.name = checkNotNull(name);
    this.type = checkNotNull(type);
  }

  /**
   * Gets the name of the field.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the Java class of the field.
   */
  public Class getType() {
    return type;
  }

  /**
   * Gets whether a value for the field must be specified.
   *
   * @see #withNotNull(boolean)
   */
  public boolean isNotNull() {
    return notNull;
  }

  /**
   * @see #isNotNull()
   */
  public FieldDefinition withNotNull(final boolean notNull) {
    this.notNull = notNull;
    return this;
  }

  /**
   * Gets whether the field should be indexed.
   *
   * @see #withIndexed(boolean)
   */
  public boolean isIndexed() {
    return indexed;
  }

  /**
   * @see #isIndexed()
   */
  public FieldDefinition withIndexed(final boolean indexed) {
    this.indexed = indexed;
    return this;
  }

  /**
   * If {@link #isIndexed()}, gets whether the field's values must be unique.
   *
   * @see #withUnique(boolean)
   */
  public boolean isUnique() {
    return unique;
  }

  /**
   * @see #withUnique(boolean)
   */
  public FieldDefinition withUnique(final boolean unique) {
    this.unique = unique;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!(obj instanceof FieldDefinition)) return false;
    final FieldDefinition other = (FieldDefinition) obj;
    return Objects.equal(getName(), other.getName())
        && Objects.equal(getType(), other.getType())
        && Objects.equal(isNotNull(), other.isNotNull())
        && Objects.equal(isIndexed(), other.isIndexed())
        && Objects.equal(isUnique(), other.isUnique());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), getType(), isNotNull(), isIndexed(), isUnique());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(FieldDefinition.class)
        .add("name", getName())
        .add("type", getType())
        .add("notNull", isNotNull())
        .add("indexed", isIndexed())
        .add("unique", isUnique())
        .toString();
  }
}
