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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataType.Attribute.Type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataType<T>
{
  private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

  private static final Set<Class> SUPPORTED_CLASSES = Sets.newHashSet(new Class[] {
      String.class, DateTime.class, Long.class, Integer.class, Enum.class,
      Character.class, Boolean.class, List.class, Map.class});

  private final String name;

  private final Class<T> metadataClass;

  private final Class metadataBuilderClass;

  private final Map<String, Attribute> attributes;

  private final Set<MetadataIndex> indexes;

  private MetadataType(String name, Class<T> metadataClass, Set<String> textAttributes, Set<MetadataIndex> indexes) {
    this.name = checkName(name);
    this.metadataClass = checkMetadataClass(metadataClass);
    this.attributes = checkAttributes(textAttributes);
    this.metadataBuilderClass = checkMetadataBuilderClass(metadataClass);
    this.indexes = checkIndexes(indexes);
  }

  // Must be 1 or more chars long and may only consist of alphanum and underscore characters
  protected static String checkName(String name) {
    checkNotNull(name);
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid name: " + name +
          "; only alphanumeric characters and underscores are allowed");
    }
    return name;
  }

  // must be IdentityMetadata or a subclass of SecondaryMetadata
  private static <T> Class<T> checkMetadataClass(Class<T> metadataClass) {
    checkNotNull(metadataClass);
    if (metadataClass == IdentityMetadata.class) {
      return metadataClass;
    }
    Class parent = metadataClass.getSuperclass();
    while (parent != null) {
      if (parent == SecondaryMetadata.class) {
        return metadataClass;
      }
      parent = parent.getSuperclass();
    }
    throw new IllegalArgumentException(metadataClass + " is not a subclass of " + SecondaryMetadata.class);
  }

  private static void checkInterface(Class theClass, Class theInterface) {
    for (Class hasInterface: theClass.getInterfaces()) {
      if (hasInterface == theInterface) {
        return;
      }
    }
    throw new IllegalArgumentException(theClass + " does not implement " + theInterface);
  }

  // Metadata class must have @JsonDeserialize annotation specifying a builder that is a MetadataBuilder
  // Builder class's declared fields must:
  // - consist only of nonprimitive types
  // - consist only of supported types
  // - consist only of fields for which there exist getters (attributes) in the corresponding metadata class
  private Class checkMetadataBuilderClass(Class<T> metadataClass) {
    JsonDeserialize jsonDeserialize = metadataClass.getAnnotation(JsonDeserialize.class);
    if (jsonDeserialize == null) {
      throw new IllegalArgumentException(metadataClass + " does not have a @JsonDeserialize annotation");
    }
    if (jsonDeserialize.builder() == null || jsonDeserialize.builder().getSimpleName().equals("NoClass")) {
      throw new IllegalArgumentException(metadataClass + "'s @JsonDeserialize annotation does not specify a builder");
    }
    Class metadataBuilderClass = jsonDeserialize.builder();
    checkInterface(metadataBuilderClass, MetadataBuilder.class);

    for (Field field: metadataBuilderClass.getDeclaredFields()) {
      Class fieldType = field.getType();
      if (fieldType.isPrimitive()) {
        throw new IllegalArgumentException("Metadata builder uses primitive type for field '" + field.getName() +
            "'. Try using the class equivalent or another class instead. Supported classes are: " + SUPPORTED_CLASSES);
      }
      if (!fieldType.isEnum() && !SUPPORTED_CLASSES.contains(fieldType)) {
        throw new IllegalArgumentException("Metadata builder uses unsupported type for field '" + field.getName() +
            "'. Supported classes are: " + SUPPORTED_CLASSES);

      }
      if (!attributes.containsKey(field.getName())) {
        throw new IllegalArgumentException("Metadata builder's field '" + field.getName() + "' has no corresponding " +
            "attribute in the associated metadata class.");
      }
    }

    return metadataBuilderClass;
  }

  // Getters (parameterless methods starting with "get" or "is") are assumed to be attributes.
  // Their storage types will be inferred from their return types and, if they are strings, whether they are
  // in the textAttributes set. If a suitable type cannot be found, a runtime exception will be thrown.
  // Their nullability will be assumed to be false unless annotated with @Nullable.
  private Map<String, Attribute> checkAttributes(Set<String> textAttributes) {
    Map<String, Attribute> attributes = new HashMap<>();
    for (Method method: metadataClass.getMethods()) {
      String name = getAttributeName(method);
      if (name != null) {
        Type type = Type.forClass(method.getReturnType(), textAttributes.contains(name));
        boolean nullable = method.isAnnotationPresent(Nullable.class);
        attributes.put(name, new Attribute(name, type, nullable));
      }
    }
    for (String textAttribute: textAttributes) {
      if (!attributes.containsKey(textAttribute)) {
        throw new IllegalArgumentException("No such attribute in " + metadataClass.getName() + ": " + textAttribute);
      }
    }
    return attributes;
  }

  @Nullable
  private static String getAttributeName(Method method) {
    if (method.getParameterTypes().length == 0) {
      String name = method.getName();
      if (name.startsWith("get") && !name.equals("getClass")) {
        return StringUtils.uncapitalize(name.substring(3));
      }
      else if (name.startsWith("is")) {
        return StringUtils.uncapitalize(name.substring(2));
      }
    }
    return null;
  }

  // Duplicate indexes are not allowed
  // All index columns must refer to existing attributes with indexable types
  // Multiple unique indexes are not allowed
  private Set<MetadataIndex> checkIndexes(Set<MetadataIndex> indexes) {
    checkNotNull(indexes);
    boolean sawUnique = false;
    for (MetadataIndex index: indexes) {
      List<String> columns = index.getColumns();
      for (String column: columns) {
        Attribute attribute = attributes.get(column);
        if (attribute == null) {
          throw new IllegalArgumentException("Index refers to non-existing attribute: " + column);
        }
        if (attribute.getType() == Type.ARRAY ||
            attribute.getType() == Type.MAP ||
            attribute.getType() == Type.TEXT) {
          throw new IllegalArgumentException("Index refers to attribute with non-indexable type: " + column);
        }
      }
      if (index.isUnique()) {
        if (sawUnique) {
          throw new IllegalArgumentException("Multiple unique indexes are not allowed");
        }
        sawUnique = true;
      }
    }
    return indexes;
  }

  public String getName() {
    return name;
  }

  public String getIndexName(MetadataIndex index) {
    if (!indexes.contains(index)) {
      throw new IllegalArgumentException("Not a '" + name + "' index: " + index);
    }
    StringBuilder builder = new StringBuilder(name);
    for (String column: index.getColumns()) {
      builder.append("-").append(column);
    }
    return builder.toString();
  }

  public Class<T> getMetadataClass() {
    return metadataClass;
  }

  public Class getMetadataBuilderClass() {
    return metadataBuilderClass;
  }

  public Map<String, Attribute> getAttributes() {
    return attributes;
  }

  public Set<MetadataIndex> getIndexes() {
    return indexes;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final MetadataType other = (MetadataType) obj;
    return Objects.equal(name, other.name)
        && Objects.equal(metadataClass, other.metadataClass)
        && Objects.equal(metadataBuilderClass, other.metadataBuilderClass)
        && Objects.equal(attributes, other.attributes)
        && Objects.equal(indexes, other.indexes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, metadataClass, metadataBuilderClass, attributes, indexes);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("name", name)
        .add("metadataClass", metadataClass)
        .add("metadataBuilderClass", metadataBuilderClass)
        .add("attributes", attributes)
        .add("indexes", indexes)
        .toString();
  }

  public static class Attribute
  {
    private final String name;
    private final Type type;
    private final boolean nullable;

    public Attribute(String name, Type type, boolean nullable) {
      this.name = checkName(name);
      this.type = checkNotNull(type);
      this.nullable = nullable;
    }

    public String getName() {
      return name;
    }

    public Type getType() {
      return type;
    }

    public boolean isNullable() {
      return nullable;
    }

    public static enum Type
    {
      MAP, ARRAY, BOOLEAN, CHAR, ENUM, INTEGER, LONG, DATE, SMALLSTRING, TEXT;

      public static Type forClass(Class attributeClass, boolean text) {
        if (attributeClass == String.class) {
          if (text) {
            return TEXT;
          }
          else {
            return SMALLSTRING;
          }
        }
        if (text) {
          throw new IllegalArgumentException(attributeClass + " is not a text-compatible class");
        }
        else if (attributeClass == DateTime.class) {
          return DATE;
        }
        else if (attributeClass == Long.class || attributeClass.toString().equals("long")) {
          return LONG;
        }
        else if (attributeClass == Integer.class || attributeClass.toString().equals("int")) {
          return INTEGER;
        }
        else if (attributeClass.isEnum()) {
          return ENUM;
        }
        else if (attributeClass == Character.class || attributeClass.toString().equals("char")) {
          return CHAR;
        }
        else if (attributeClass == Boolean.class || attributeClass.toString().equals("boolean")) {
          return BOOLEAN;
        }
        else if (attributeClass == List.class) {
          return ARRAY;
        }
        else if (attributeClass == Map.class) {
          return MAP;
        }
        else {
          throw new IllegalArgumentException(attributeClass + " is not a supported attribute class");
        }
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Attribute other = (Attribute) obj;
      return Objects.equal(name, other.name)
          && Objects.equal(type, other.type)
          && Objects.equal(nullable, other.nullable);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, type, nullable);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("name", name)
          .add("type", type)
          .add("nullable", nullable)
          .toString();
    }
  }

  public static class Builder<T> {
    private String name;
    private Class<T> metadataClass;
    private Set<String> textAttributes = new HashSet<>();
    private Set<MetadataIndex> indexes = new HashSet<>();

    public Builder<T> withName(String name) {
      this.name = name;
      return this;
    }

    public Builder<T> withMetadataClass(Class<T> metadataClass) {
      this.metadataClass = metadataClass;
      return this;
    }

    public Builder<T> withTextAttributes(String... textAttributes) {
      Collections.addAll(this.textAttributes, textAttributes);
      return this;
    }

    public Builder<T> withIndexes(MetadataIndex... indexes) {
      Collections.addAll(this.indexes, indexes);
      return this;
    }

    public MetadataType<T> build() {
      return new MetadataType<T>(name, metadataClass, textAttributes, indexes);
    }
  }
}
