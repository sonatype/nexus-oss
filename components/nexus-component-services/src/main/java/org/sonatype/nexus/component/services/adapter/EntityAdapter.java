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
package org.sonatype.nexus.component.services.adapter;

import org.sonatype.nexus.component.model.Entity;

import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Adapter for converting between {@link Entity} objects and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public interface EntityAdapter<T extends Entity>
{
  /** OrientDB property name for an entity's globally unique id. */
  static final String P_ID = "id";

  /**
   * Gets the entity class this adapter works with.
   */
  Class<T> getEntityClass();

  /**
   * Creates the OrientDB class and associated indexes, if needed.
   */
  void createStorageClass(OSchema schema);

  /**
   * Sets the given document's type-specific properties based on those of the given entity.
   */
  void populateDocument(T entity, ODocument document);

  /**
   * Sets the given entity's type-specific properties based on those of the given document.
   */
  void populateEntity(ODocument document, T entity);
}
