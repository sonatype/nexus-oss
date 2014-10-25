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
package org.sonatype.nexus.component.services.internal.query;

import java.util.Map;

import org.sonatype.nexus.component.services.internal.adapter.AssetEntityAdapter;
import org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction;
import org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction.EntityType;
import org.sonatype.nexus.component.services.query.CompoundMetadataQueryRestriction;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.id.ORID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Translates a {@link MetadataQuery} into OrientDB syntax.
 *
 * @since 3.0
 */
public class OrientQueryBuilder
{
  private final MetadataQuery query;

  private final ORID skipRid;

  private final Map<String, Object> parameters = Maps.newHashMap();

  /**
   * Creates an instance with the given query.
   */
  public OrientQueryBuilder(MetadataQuery query) {
    this.query = checkNotNull(query);
    this.skipRid = null;

    checkArgument(query.skipComponentId() == null,
        "Metadata query specifies a skipComponentId; OrientDB query must specify a skipRid");
  }

  /**
   * Creates an instance with the given query, with a skipRid specified for paging.
   */
  public OrientQueryBuilder(MetadataQuery query, ORID skipRid) {
    this.query = checkNotNull(query);
    this.skipRid = checkNotNull(skipRid);

    checkArgument(query.skipComponentId() != null,
        "Metadata query does not specify a skipComponentId; OriendDB query must not specify a skipRid");
  }

  /**
   * Gets the parameter name-value map corresponding to the last built query.
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }

  /**
   * Gets the query text that will select matching assets.
   */
  public String buildAssetQuery(boolean isCountQuery) {
    parameters.clear();
    return buildQuery(AssetEntityAdapter.ORIENT_CLASS_NAME, isCountQuery, false);
  }

  /**
   * Gets the query text that will select matching components.
   */
  public String buildComponentQuery(String componentClassName, boolean isCountQuery) {
    parameters.clear();
    return buildQuery(checkNotNull(componentClassName), isCountQuery, true);
  }

  private String buildQuery(String className, boolean isCountQuery, boolean isComponentQuery) {

    // "SELECT [COUNT(*)] FROM componentClassName"

    StringBuilder builder = new StringBuilder("SELECT ");
    if (isCountQuery) {
      builder.append("COUNT(*) ");
    }
    builder.append("FROM ") ;
    builder.append(className);

    // "[ WHERE [@rid > lowerRid] [ AND ] [restriction] ]"

    StringBuilder whereBuilder = null;

    if (skipRid != null) {
      whereBuilder = new StringBuilder("@rid > ").append(skipRid.toString());
    }
    if (query.restriction() != null) {
      if (whereBuilder == null) {
        whereBuilder = new StringBuilder();
      }
      else {
        whereBuilder.append(" AND ");
      }
      appendRestriction(whereBuilder, query.restriction(), isComponentQuery);
    }
    if (whereBuilder != null) {
      builder.append(" WHERE ");
      builder.append(whereBuilder.toString());
    }

    if (!isCountQuery) {

      // "[ ORDER BY prop1 ASC|DESC [, prop2 ASC|DESC [, ..]]]"

      if (!query.orderBy().isEmpty()) {
        builder.append(" ORDER BY ");
        boolean first = true;
        for (String propertyName: query.orderBy().keySet()) {
          if (!first) {
            builder.append(", ");
          }
          else {
            first = false;
          }
          builder.append(propertyName);
          boolean ascending = query.orderBy().get(propertyName);
          if (ascending) {
            builder.append(" ASC");
          }
          else {
            builder.append(" DESC");
          }
        }
      }

      // "[ SKIP skip]"

      if (query.skip() != null) {
        builder.append(" SKIP ").append(query.skip());
      }

      // "[ LIMIT limit]"

      if (query.limit() != null) {
        builder.append(" LIMIT ").append(query.limit());
      }
    }

    return builder.toString();
  }

  private void appendRestriction(StringBuilder builder, MetadataQueryRestriction restriction, boolean isComponentQuery) {
    if (restriction instanceof BooleanMetadataQueryRestriction) {
      appendBooleanRestriction(builder, (BooleanMetadataQueryRestriction) restriction, isComponentQuery);
    }
    else if (restriction instanceof CompoundMetadataQueryRestriction) {
      appendCompoundRestriction(builder, (CompoundMetadataQueryRestriction) restriction, isComponentQuery);
    }
  }

  /**
   * Appends a boolean restriction using an operator such as = or LIKE.
   */
  private void appendBooleanRestriction(StringBuilder builder, BooleanMetadataQueryRestriction booleanRestriction, boolean isComponentQuery) {
    final String expression = booleanRestriction.getName()
        + " " + orientOperator(booleanRestriction.getOperator())
        + " :" + createParameter(booleanRestriction.getValue());

    if (booleanRestriction.getEntityType() == EntityType.COMPONENT) {
      if (isComponentQuery) {
        // Component restriction in a query for components:
        // "propertyName operator value"
        builder.append(expression);
      }
      else {
        // Component restriction in a query for assets:
        // "component.propertyName operator value"
        builder.append("component.").append(expression);
      }
    }
    else {
      if (isComponentQuery) {
        // Asset restriction in a query for components:
        // "assets contains ( propertyName operator value )"
        builder.append("assets contains ( ").append(expression).append(" )");
      }
      else {
        // Asset restriction in a query for assets:
        // "propertyName operator value"
        builder.append(expression);
      }
    }
  }

  private String createParameter(Object value) {
    String name = "param" + (parameters.size() + 1);
    parameters.put(name, value);
    return name;
  }

  /**
   * Appends a compound restriction using an operator such as AND or OR.
   */
  private void appendCompoundRestriction(StringBuilder builder, CompoundMetadataQueryRestriction compoundRestriction, boolean isComponentQuery) {
    String operator = orientOperator(compoundRestriction.getOperator());

    // "( restriction1 operator restriction2 [ operator restriction3 [..]] )"
    builder.append('(');
    boolean first = true;
    for (MetadataQueryRestriction restriction: compoundRestriction.operands()) {
      if (!first) {
        builder.append(' ').append(operator).append(' ');
      }
      else {
        first = false;
      }
      appendRestriction(builder, restriction, isComponentQuery);
    }
    builder.append(')');
  }

  private static String orientOperator(CompoundMetadataQueryRestriction.Operator compoundOperator) {
    if (compoundOperator == CompoundMetadataQueryRestriction.Operator.AND) {
      return "AND";
    }
    else if (compoundOperator == CompoundMetadataQueryRestriction.Operator.OR) {
      return "OR";
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  private static String orientOperator(BooleanMetadataQueryRestriction.Operator booleanOperator) {
    if (booleanOperator == BooleanMetadataQueryRestriction.Operator.EQ) {
      return "=";
    }
    else if (booleanOperator == BooleanMetadataQueryRestriction.Operator.LIKE) {
      return "LIKE";
    }
    else {
      throw new UnsupportedOperationException();
    }
  }
}
