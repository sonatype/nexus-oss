/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.common.entity;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Entity helpers.
 *
 * @since 3.0
 */
public class EntityHelper
{
  private EntityHelper() {}

  @Nonnull
  public static EntityMetadata metadata(final Entity entity) {
    checkNotNull(entity);
    EntityMetadata metadata = entity.getEntityMetadata();
    checkState(metadata != null, "Missing entity-metadata");
    //noinspection ConstantConditions
    return metadata;
  }

  @Nonnull
  public static EntityId id(final Entity entity) {
    EntityId id = metadata(entity).getId();
    // sanity id should never be null
    checkState(id != null, "Missing entity-id");
    //noinspection ConstantConditions
    return id;
  }

  @Nonnull
  public static EntityVersion version(final Entity entity) {
    EntityVersion version = metadata(entity).getVersion();
    // sanity version should never be null
    checkState(version != null, "Missing entity-version");
    //noinspection ConstantConditions
    return version;
  }
}
