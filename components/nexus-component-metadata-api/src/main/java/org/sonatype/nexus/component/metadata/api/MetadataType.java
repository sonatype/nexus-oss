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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.metadata.api.MetadataType.Attribute.Type;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataType
{
  private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

  private final String name;

  private final Class pojoClass;

  private final Map<String, Attribute> attributes;

  private final Set<Index> indexes;

  private MetadataType(String name, Class pojoClass, Set<String> textAttributes, Set<Index> indexes) {
    this.name = checkName(name);
    this.pojoClass = checkPojoClass(pojoClass);
    this.attributes = checkAttributes(textAttributes);
    this.indexes = checkIndexes(indexes);
  }

  // Must be 1 or more chars long and may only consist of alphanum and underscore characters
  private static String checkName(String name) {
    checkNotNull(name);
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid name: " + name +
          "; only alphanumeric characters and underscores are allowed");
    }
    return name;
  }

  // Must be IdentityMetadata or implement SecondaryMetadata
  private static Class checkPojoClass(Class pojoClass) {
    checkNotNull(pojoClass);
    if (pojoClass != IdentityMetadata.class) {
      for (Class iface: pojoClass.getInterfaces()) {
        if (iface == SecondaryMetadata.class) {
          throw new IllegalArgumentException(pojoClass.getName() + " does not implement SecondaryMetadata");
        }
      }
    }
    return pojoClass;
  }

  // Getters (parameterless methods starting with "get" or "is") are assumed to be attributes.
  // Their storage types will be inferred from their return types and, if they are strings, whether they are
  // in the textAttributes set. If a suitable type cannot be found, a runtime exception will be thrown.
  // Their nullability will be assumed to be false unless annotated with @Nullable.
  // At least one attribute must exist among all methods of the class.
  private Map<String, Attribute> checkAttributes(Set<String> textAttributes) {
    Map<String, Attribute> attributes = new HashMap<>();
    for (Method method: pojoClass.getMethods()) {
      String name = getAttributeName(method);
      if (name != null) {
        Type type = Type.forClass(method.getReturnType(), textAttributes.contains(name));
        boolean nullable = method.isAnnotationPresent(Nullable.class);
        attributes.put(name, new Attribute(name, type, nullable));
      }
    }
    if (attributes.size() == 0) {
      throw new IllegalArgumentException(pojoClass.getName() + " does not implement any getters");
    }
    for (String textAttribute: textAttributes) {
      if (!attributes.containsKey(textAttribute)) {
        throw new IllegalArgumentException("No such attribute in " + pojoClass.getName() + ": " + textAttribute);
      }
    }
    return attributes;
  }

  @Nullable
  private static String getAttributeName(Method method) {
    if (method.getParameterTypes().length == 0) {
      String name = method.getName();
      if (name.startsWith("get")) {
        return StringUtils.uncapitalize(name.substring(3));
      }
      else if (name.startsWith("is")) {
        return StringUtils.uncapitalize(name.substring(3));
      }
    }
    return null;
  }

  // Duplicate indexes are not allowed
  // All index columns must refer to existing attributes with indexable types
  // Multiple unique indexes are not allowed
  private Set<Index> checkIndexes(Set<Index> indexes) {
    checkNotNull(indexes);
    boolean sawUnique = false;
    Set<List<String>> sawColumns = new HashSet<>();
    for (Index index: indexes) {
      List<String> columns = index.getColumns();
      if (sawColumns.contains(columns)) {
        throw new IllegalArgumentException("Duplicate index specified: " + columns);
      }
      sawColumns.add(columns);
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

  public Class getPojoClass() {
    return pojoClass;
  }

  public Map<String, Attribute> getAttributes() {
    return attributes;
  }

  public Set<Index> getIndexes() {
    return indexes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "name=" + name +
        ", pojoClass=" + pojoClass +
        ", indexes=" + indexes +
        "}";
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

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "name=" + name +
          "nullable=" + nullable +
          "}";
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
        else if (attributeClass.isArray() || attributeClass == List.class) {
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
  }

  public static class Index
  {
    private final List<String> columns;
    private final boolean unique;
    private final boolean descending;

    private Index(List<String> columns, boolean unique, boolean descending) {
      this.columns = ImmutableList.copyOf(checkNotNull(columns));
      this.unique = unique;
      this.descending = descending;
    }

    private static List<String> checkColumns(List<String> columns) {
      checkNotNull(columns);
      if (columns.size() == 0) {
        throw new IllegalArgumentException("Index does not specify any columns");
      }
      Set<String> checkedColumns = new HashSet<>();
      for (String column: columns) {
        checkName(column);
        if (checkedColumns.contains(column)) {
          throw new IllegalArgumentException("Index specifies multiple columns with the same name: " + column);
        }
        checkedColumns.add(column);
      }
      return columns;
    }

    public List<String> getColumns() {
      return columns;
    }

    public boolean isUnique() {
      return unique;
    }

    public boolean isDescending() {
      return descending;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "columns=" + columns +
          ", unique=" + unique +
          ", descending=" + descending +
          "}";
    }

    public static class Builder {
      private List<String> columns = new ArrayList<>();
      private boolean unique;
      private boolean descending;

      public Builder withColumns(String... columns) {
        Collections.addAll(this.columns, columns);
        return this;
      }

      public Builder unique() {
        unique = true;
        return this;
      }

      public Builder descending() {
        descending = true;
        return this;
      }

      public Index build() {
        return new Index(columns, unique, descending);
      }
    }
  }

  public static class Builder {
    private String name;
    private Class pojoClass;
    private Set<String> textAttributes = new HashSet<>();
    private Set<Index> indexes = new HashSet<>();

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withPojoClass(Class pojoClass) {
      this.pojoClass = pojoClass;
      return this;
    }

    public Builder withTextAttributes(String... textAttributes) {
      Collections.addAll(this.textAttributes, textAttributes);
      return this;
    }

    public Builder withIndexes(Index... indexes) {
      Collections.addAll(this.indexes, indexes);
      return this;
    }

    public MetadataType build() {
      return new MetadataType(name, pojoClass, textAttributes, indexes);
    }
  }
}
