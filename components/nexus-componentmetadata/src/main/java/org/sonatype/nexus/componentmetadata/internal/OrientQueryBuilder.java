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
package org.sonatype.nexus.componentmetadata.internal;

import org.sonatype.nexus.componentmetadata.RecordQuery;
import org.sonatype.nexus.orient.RecordIdObfuscator;

import com.orientechnologies.orient.core.metadata.schema.OClass;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to convert a {@link RecordQuery} into an OrientDB SELECT statement.
 *
 * @since 3.0
 */
class OrientQueryBuilder
{
  private final RecordQuery query;

  private final RecordIdObfuscator recordIdObfuscator;

  private final OClass oClass;

  private boolean count;

  /**
   * @param query the source query.
   * @param oClass the Orient class corresponding to the type specified by the query.
   * @param recordIdObfuscator the id obfuscator to use when decoding record ids.
   */
  public OrientQueryBuilder(RecordQuery query,
                            OClass oClass,
                            RecordIdObfuscator recordIdObfuscator) {
    this.query = checkNotNull(query);
    this.oClass = checkNotNull(oClass);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  /**
   * Specifies whether this is a COUNT query.
   */
  public OrientQueryBuilder withCount(boolean count) {
    this.count = count;
    return this;
  }

  /**
   * Builds the OrientDB SELECT statement.
   */
  public String build() {
    // SELECT [COUNT(*)] FROM typeName

    StringBuilder builder = new StringBuilder("SELECT ");
    if (count) {
      builder.append("COUNT(*) ");
    }
    builder.append("FROM ");
    builder.append(query.getType().getName());

    // [ WHERE [@rid > lowerRid] [[ AND ] filterKey1 = :filterKey1 [AND filterKey2 = :filterKey2 [..]]]]

    StringBuilder whereBuilder = null;

    if (query.getSkipRecord() != null) {
      String ridString = recordIdObfuscator.decode(oClass, query.getSkipRecord().getValue()).toString();
      whereBuilder = new StringBuilder(" WHERE @rid > ").append(ridString);
    }
    if (query.getFilter().size() > 0) {
      for (String key : query.getFilter().keySet()) {
        if (whereBuilder == null) {
          whereBuilder = new StringBuilder(" WHERE ");
        }
        else {
          whereBuilder.append(" AND ");
        }
        whereBuilder.append(key).append(" = :").append(key);
      }
    }
    if (whereBuilder != null) {
      builder.append(whereBuilder.toString());
    }

    // [ ORDER BY orderBy [DESC]] [ SKIP skip] [ LIMIT limit]

    if (!count) {
      if (query.getOrderBy() != null) {
        builder.append(" ORDER BY ").append(query.getOrderBy());
        if (query.isDescending()) {
          builder.append(" DESC");
        }
      }
      if (query.getSkip() != null) {
        builder.append(" SKIP ").append(query.getSkip());
      }
      if (query.getLimit() != null) {
        builder.append(" LIMIT ").append(query.getLimit());
      }
    }

    return builder.toString();
  }
}
