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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataIndex
{
  private final List<String> columns;
  private final boolean unique;
  private final boolean descending;

  private MetadataIndex(List<String> columns, boolean unique, boolean descending) {
    this.columns = checkColumns(columns);
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
      MetadataType.checkName(column);
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
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final MetadataIndex other = (MetadataIndex) obj;
    return Objects.equal(columns, other.columns)
        && Objects.equal(unique, other.unique)
        && Objects.equal(descending, other.descending);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(columns, unique, descending);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("columns", columns)
        .add("unique", unique)
        .add("descending", descending)
        .toString();
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

    public MetadataIndex build() {
      return new MetadataIndex(columns, unique, descending);
    }
  }
}
