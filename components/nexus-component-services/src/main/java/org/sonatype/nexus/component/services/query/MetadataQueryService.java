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
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Entity;

/**
 * Search interface for finding components and assets.
 *
 * Results represent the state of the stored items at the time of the query are are divorced from the session
 * from which they originated by the time they are returned.
 *
 * @since 3.0
 */
public interface MetadataQueryService
{
  /**
   * Gets all entity classes known to the service.
   */
  Set<Class<? extends Entity>> entityClasses();

  /**
   * Gets the number of entities of the given class matching the given restriction.
   */
  <T extends Entity> long count(Class<T> entityClass, @Nullable MetadataQueryRestriction restriction);

  /**
   * Gets the list of entities of the given class matching the given query.
   */
  <T extends Entity> List<T> find(Class<T> entityClass, @Nullable MetadataQuery metadataQuery);
}
