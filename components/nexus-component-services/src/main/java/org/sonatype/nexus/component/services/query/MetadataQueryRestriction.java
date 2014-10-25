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

import static org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction.EntityType.ASSET;
import static org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction.EntityType.COMPONENT;
import static org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction.Operator.EQ;
import static org.sonatype.nexus.component.services.query.BooleanMetadataQueryRestriction.Operator.LIKE;
import static org.sonatype.nexus.component.services.query.CompoundMetadataQueryRestriction.Operator.AND;
import static org.sonatype.nexus.component.services.query.CompoundMetadataQueryRestriction.Operator.OR;

/**
 * Base class for metadata query restrictions, providing static helpers to construct instances.
 *
 * @since 3.0
 */
public abstract class MetadataQueryRestriction
{
  /**
   * Creates a restriction specifying that an asset property must have some value.
   */
  public static BooleanMetadataQueryRestriction assetPropertyEquals(String name, Object value) {
    return new BooleanMetadataQueryRestriction(EQ, ASSET, name, value);
  }

  /**
   * Creates a restriction specifying that an asset property must be LIKE some pattern (using % as a placeholder).
   */
  public static BooleanMetadataQueryRestriction assetPropertyLike(String name, String pattern) {
    return new BooleanMetadataQueryRestriction(LIKE, ASSET, name, pattern);
  }

  /**
   * Creates a restriction specifying that a component property must have some value.
   */
  public static BooleanMetadataQueryRestriction componentPropertyEquals(String name, Object value) {
    return new BooleanMetadataQueryRestriction(EQ, COMPONENT, name, value);
  }

  /**
   * Creates a restriction specifying that a component property must be LIKE some pattern (using % as a placeholder).
   */
  public static BooleanMetadataQueryRestriction componentPropertyLike(String name, String pattern) {
    return new BooleanMetadataQueryRestriction(LIKE, COMPONENT, name, pattern);
  }

  /**
   * Creates a restriction that is the logical AND of the given restrictions.
   */
  public static CompoundMetadataQueryRestriction and(MetadataQueryRestriction... operands) {
    return new CompoundMetadataQueryRestriction(AND, operands);
  }

  /**
   * Creates a restriction that is the logical OR of the given restrictions.
   */
  public static CompoundMetadataQueryRestriction or(MetadataQueryRestriction... operands) {
    return new CompoundMetadataQueryRestriction(OR, operands);
  }
}
