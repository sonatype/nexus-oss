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

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Specifies a query against a {@link RecordStore}.
 *
 * @since 3.0
 */
public class RecordQuery
{
  private final RecordType type;

  private final Map<String, Object> filter = Maps.newLinkedHashMap();

  private Integer limit;

  private Integer skip;

  private RecordId skipRecord;

  private String orderBy;

  private boolean descending;

  public RecordQuery(RecordType type) {
    this.type = checkNotNull(type);
  }

  public RecordType getType() {
    return type;
  }

  public RecordQuery withEqual(String key, Object value) {
    checkArgument(!(checkNotNull(key).isEmpty()), "Key cannot be empty");
    checkNotNull(value);
    filter.put(key, value);
    return this;
  }

  /**
   * Gets a map containing all the key value pairs added via {@link #withEqual(String, Object)}.
   *
   * Iterating over the keys of this map will provide them in insert order.
   */
  public Map<String, Object> getFilter() {
    return filter;
  }

  public RecordQuery withSkip(Integer skip) {
    checkArgument(checkNotNull(skip) >= 0, "Skip must be non-negative");
    checkArgument(skipRecord == null, "Cannot skip; skip record is already specified");
    this.skip = skip;
    return this;
  }

  @Nullable
  public Integer getSkip() {
    return skip;
  }

  public RecordQuery withSkipRecord(RecordId skipRecord) {
    checkNotNull(skipRecord);
    checkArgument(skip == null, "Cannot skip record; skip is already specified");
    checkArgument(orderBy == null, "Cannot skip record; order by is already specified");
    this.skipRecord = skipRecord;
    return this;
  }

  @Nullable
  public RecordId getSkipRecord() {
    return skipRecord;
  }

  public RecordQuery withOrderBy(String orderBy) {
    checkArgument(!(checkNotNull(orderBy).isEmpty()), "Order by cannot be empty");
    checkArgument(skipRecord == null, "Cannot order by; skip record is already specified");
    this.orderBy = orderBy;
    return this;
  }

  @Nullable
  public String getOrderBy() {
    return orderBy;
  }

  public RecordQuery withLimit(Integer limit) {
    checkArgument(checkNotNull(limit) > 0, "Limit must be positive");
    this.limit = limit;
    return this;
  }

  @Nullable
  public Integer getLimit() {
    return this.limit;
  }

  public RecordQuery withDescending(boolean descending) {
    this.descending = descending;
    return this;
  }

  public boolean isDescending() {
    return descending;
  }
}
