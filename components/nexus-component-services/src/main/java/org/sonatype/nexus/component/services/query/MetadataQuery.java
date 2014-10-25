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
package org.sonatype.nexus.component.services.query;

import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.ComponentId;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a query for component or asset metadata.
 *
 * @since 3.0
 */
public class MetadataQuery
{
  private Integer limit;

  private Integer skip;

  private ComponentId skipComponentId;

  private String skipAssetPath;

  private Map<String, Boolean> orderBy = Maps.newLinkedHashMap();

  private MetadataQueryRestriction restriction;

  /**
   * Creates a query with no restrictions, order, limit, or skip specified.
   */
  public MetadataQuery() {
  }

  /**
   * Sets the maximum number of results the query may return.
   */
  public MetadataQuery limit(Integer limit) {
    checkArgument(checkNotNull(limit) > 0, "Limit must be positive");
    this.limit = limit;
    return this;
  }

  /**
   * Gets the maximum number of results the query may return, or {@code null} if unspecified.
   */
  @Nullable
  public Integer limit() {
    return limit;
  }

  /**
   * Sets the number of results to skip for paging.
   */
  public MetadataQuery skip(Integer skip) {
    checkArgument(checkNotNull(skip) >= 0, "Skip must be non-negative");
    checkArgument(skipComponentId == null, "Cannot skip; skip component id is already specified");
    this.skip = skip;
    return this;
  }

  /**
   * Gets the number of results to skip for paging, or {@code null} if unspecified.
   */
  @Nullable
  public Integer skip() {
    return skip;
  }

  /**
   * Sets the component id to skip for paging.
   *
   * Entity based paging is faster than skip-limit based paging, but it is only useful if the query results
   * occur in their natural (insertion) order. Callers should therefore be careful not to specify any {@code orderBy}
   * fields in conjunction with this, and should be careful to avoid using it with queries where the use of an index
   * may cause results to be iterated in non-natural order.
   */
  public MetadataQuery skipComponentId(ComponentId skipComponentId) {
    checkNotNull(skipComponentId);
    checkArgument(skip == null, "Cannot skip component id; skip is already specified");
    checkArgument(orderBy.isEmpty(), "Cannot skip component id; order by is already specified");
    this.skipComponentId = skipComponentId;
    return this;
  }

  /**
   * Gets the component id to skip for paging, or {@code null} if unspecified.
   */
  @Nullable
  public ComponentId skipComponentId() {
    return skipComponentId;
  }

  /**
   * Sets the asset path to skip for asset paging.
   *
   * When this is specified, a {@code skipComponentId} must also be specified in order to fully qualify the
   * asset to be skipped.
   *
   * @see #skipComponentId(ComponentId)
   */
  public MetadataQuery skipAssetPath(String skipAssetPath) {
    checkNotNull(skipAssetPath);
    checkArgument(skip == null, "Cannot skip asset path; skip is already specified");
    checkArgument(orderBy.isEmpty(), "Cannot skip asset path; order by is already specified");
    this.skipAssetPath = skipAssetPath;
    return this;
  }

  /**
   * Gets the asset path to skip for asset paging, or {@code null} if unspecified.
   */
  @Nullable
  public String skipAssetPath() {
    return skipAssetPath;
  }

  /**
   * Specifies a field and direction to order results by.
   *
   * This may be called multiple times to specify additional properties to order by when these property values are
   * equivalent.
   */
  public MetadataQuery orderBy(String propertyName, boolean ascending) {
    checkArgument(!Strings.isNullOrEmpty(propertyName), "Order by property name cannot be empty");
    checkArgument(skipComponentId == null, "Cannot order by; skip component id is already specified");
    checkArgument(skipAssetPath == null, "Cannot order by; skip asset path is already specified");
    this.orderBy.put(propertyName, ascending);
    return this;
  }

  /**
   * Gets the {@code orderBy} properties in insertion order, possibly empty, never {@code null}.
   *
   * Values are property names, keys are whether ascending order (true) or descending order (false) is wanted.
   */
  public Map<String, Boolean> orderBy() {
    return orderBy;
  }

  /**
   * Sets the restriction of the query, programmatically specifying a WHERE clause.
   */
  public MetadataQuery restriction(MetadataQueryRestriction restriction) {
    checkNotNull(restriction);
    this.restriction = restriction;
    return this;
  }

  /**
   * Gets the restriction of the query, or {@code null} if it is unrestricted (the default).
   */
  @Nullable
  public MetadataQueryRestriction restriction() {
    return restriction;
  }
}
