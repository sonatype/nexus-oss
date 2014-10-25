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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A restriction that specifies a property constraint.
 *
 * @since 3.0
 */
public class BooleanMetadataQueryRestriction
    extends MetadataQueryRestriction
{
  public enum Operator {
    EQ,
    LIKE
  }

  public enum EntityType
  {
    ASSET,
    COMPONENT
  }

  private final Operator operator;

  private final EntityType entityType;

  private final String name;

  private final Object value;

  BooleanMetadataQueryRestriction(Operator operator, EntityType entityType, String name, Object value) {
    this.operator = checkNotNull(operator);
    this.entityType = checkNotNull(entityType);
    this.name = checkNotNull(name);
    this.value = checkNotNull(value);
  }

  public Operator getOperator() {
    return operator;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }
}
