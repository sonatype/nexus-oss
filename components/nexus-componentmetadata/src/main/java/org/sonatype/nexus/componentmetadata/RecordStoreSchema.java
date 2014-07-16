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

/**
 * Manages the {@link RecordType}es associated with a {@link RecordStore}.
 *
 * @since 3.0
 */
public interface RecordStoreSchema
    extends Iterable<RecordType>
{
  /**
   * Conditionally adds a type for use with the store.
   *
   * @return whether the type was added. It will not be added if a type with the given name already exists.
   */
  boolean addType(RecordType type);

  /**
   * Gets the type with the given name.
   *
   * @throws IllegalArgumentException if a type with the given name does not exist.
   */
  RecordType getType(String name);

  /**
   * Tells whether a type with the given name exists.
   */
  boolean hasType(String name);

  /**
   * Removes the given type from the schema. All associated records and indexes will be removed.
   *
   * @throws IllegalArgumentException if a type with the given name does not exist.
   */
  void dropType(String name);
}
