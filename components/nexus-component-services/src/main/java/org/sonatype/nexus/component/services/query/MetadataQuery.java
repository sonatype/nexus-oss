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

import java.util.List;

import org.sonatype.nexus.component.model.ComponentId;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataQuery
{
  private Integer limit;

  private Integer skip;

  private ComponentId skipComponentId;

  private String skipAssetPath;

  private List<String> orderBy = Lists.newArrayList();

  private boolean descending;

  private QueryExpression queryExpression;

  public MetadataQuery() {
  }

  public MetadataQuery withLimit(Integer limit) {
    checkArgument(checkNotNull(limit) > 0, "Limit must be positive");
    this.limit = limit;
    return this;
  }

  public Integer getLimit() {
    return limit;
  }

  public MetadataQuery withSkip(Integer skip) {
    checkArgument(checkNotNull(skip) >= 0, "Skip must be non-negative");
    checkArgument(skipComponentId == null, "Cannot skip; skip component id is already specified");
    this.skip = skip;
    return this;
  }

  public Integer getSkip() {
    return skip;
  }

  public MetadataQuery withSkipComponentId(ComponentId skipComponentId) {
    checkNotNull(skipComponentId);
    checkArgument(skip == null, "Cannot skip component id; skip is already specified");
    checkArgument(orderBy.isEmpty(), "Cannot skip component id; order by is already specified");
    this.skipComponentId = skipComponentId;
    return this;
  }

  public ComponentId getSkipComponentId() {
    return skipComponentId;
  }

  public MetadataQuery withSkipAssetPath(String skipAssetPath) {
    checkNotNull(skipAssetPath);
    checkArgument(skip == null, "Cannot skip asset path; skip is already specified");
    checkArgument(orderBy.isEmpty(), "Cannot skip asset path; order by is already specified");
    this.skipAssetPath = skipAssetPath;
    return this;
  }

  public String getSkipAssetPath() {
    return skipAssetPath;
  }

  public MetadataQuery withOrderBy(String orderBy) {
    checkArgument(!(checkNotNull(orderBy).isEmpty()), "Order by cannot be empty");
    checkArgument(skipComponentId == null, "Cannot order by; skip component id is already specified");
    checkArgument(skipAssetPath == null, "Cannot order by; skip asset path is already specified");
    this.orderBy.add(orderBy);
    return this;
  }

  public List<String> getOrderBy() {
    return orderBy;
  }

  public MetadataQuery withDescending(boolean descending) {
    this.descending = descending;
    return this;
  }

  public boolean isDescending() {
    return descending;
  }

  public MetadataQuery withQueryExpression(QueryExpression queryExpression) {
    checkNotNull(queryExpression);
    this.queryExpression = queryExpression;
    return this;
  }

  public QueryExpression getQueryExpression() {
    return queryExpression;
  }
}
