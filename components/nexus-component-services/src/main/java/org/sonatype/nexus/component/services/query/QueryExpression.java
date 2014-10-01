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

import org.sonatype.nexus.component.services.query.BooleanExpression.EntityType;

public abstract class QueryExpression
{
  public static BooleanExpression assetFieldEquals(String name, Object value) {
    return new BooleanExpression(BooleanExpression.Operator.EQ, EntityType.ASSET, name, value);
  }

  public static BooleanExpression assetFieldLike(String name, String pattern) {
    return new BooleanExpression(BooleanExpression.Operator.LIKE, EntityType.ASSET, name, pattern);
  }

  public static BooleanExpression componentFieldEquals(String name, Object value) {
    return new BooleanExpression(BooleanExpression.Operator.EQ, EntityType.COMPONENT, name, value);
  }

  public static BooleanExpression componentFieldLike(String name, String pattern) {
    return new BooleanExpression(BooleanExpression.Operator.LIKE, EntityType.COMPONENT, name, pattern);
  }

  public static CompoundExpression and(QueryExpression... operands) {
    return new CompoundExpression(CompoundExpression.Operator.AND, operands);
  }

  public static CompoundExpression or(QueryExpression... operands) {
    return new CompoundExpression(CompoundExpression.Operator.OR, operands);
  }
}
