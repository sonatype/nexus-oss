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

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Defines a type of record.
 *
 * @since 3.0
 */
public class RecordType
    implements Iterable<FieldDefinition>
{
  private final Map<String, FieldDefinition> fields = Maps.newLinkedHashMap();

  private final String name;

  private boolean strict;

  private RecordType superType;

  private boolean isAbstract;

  public RecordType(String name) {
    this.name = checkNotNull(name);
  }

  /**
   * Gets the name of the type.
   */
  public String getName() {
    return name;
  }

  /**
   * Adds the given field.
   *
   * @throws IllegalStateException if a field with the same name already exists.
   */
  public RecordType withField(FieldDefinition field) {
    checkState(!fields.containsKey(field.getName()), "Field '%s' is already defined for %s", field.getName(), name);
    fields.put(field.getName(), field);
    return this;
  }

  /**
   * @see #isStrict()
   */
  public RecordType withStrict(boolean strict) {
    this.strict = strict;
    return this;
  }

  /**
   * Gets whether the records of this type must only consist of fields that have been defined for it.
   *
   * @see #withStrict(boolean)
   */
  public boolean isStrict() {
    return strict;
  }

  /**
   * @see #getSuperType()
   */
  public RecordType withSuperType(RecordType superType) {
    this.superType = superType;
    return this;
  }

  /**
   * Gets the supertype, or {@code null} if one has not been defined for this type.
   */
  @Nullable
  public RecordType getSuperType() {
    return superType;
  }

  /**
   * @see #isAbstract()
   */
  public RecordType withAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
    return this;
  }

  /**
   * Gets whether the type has been declared as abstract or not.
   *
   * If records only need to stored at the subtype level, it's more efficient to declare the supertype as abstract
   * so storage doesn't need to be allocated for it.
   */
  public boolean isAbstract() {
    return isAbstract;
  }

  /**
   * Gets an iterator over all explicitly-defined fields for this type.
   */
  @Override
  public Iterator<FieldDefinition> iterator() {
    return fields.values().iterator();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!(obj instanceof RecordType)) return false;
    final RecordType other = (RecordType) obj;
    return Objects.equal(getName(), other.getName())
        && Objects.equal(fields, other.fields)
        && Objects.equal(isStrict(), other.isStrict())
        && Objects.equal(getSuperType(), other.getSuperType())
        && Objects.equal(isAbstract(), other.isAbstract());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), fields, isStrict(), getSuperType(), isAbstract());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(RecordType.class)
        .add("name", getName())
        .add("fields", fields)
        .add("strict", isStrict())
        .add("superType", getSuperType())
        .add("abstract", isAbstract())
        .toString();
  }
}
