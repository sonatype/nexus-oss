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

import java.util.List;

import javax.annotation.Nullable;

/**
 * A session for working with records in a {@link RecordStore}.
 *
 * @since 3.0
 */
public interface RecordStoreSession
    extends AutoCloseable
{
  /**
   * Gets the schema.
   */
  RecordStoreSchema getSchema();

  /**
   * Creates a record. It will not be persisted until it or the record it resides within is {@link Record#save()}d.
   *
   * @throws IllegalArgumentException if the type does not exist or is abstract.
   */
  Record create(RecordType type);

  /**
   * Gets a record, or {@code null} if it doesn't exist.
   */
  @Nullable
  Record get(RecordId id);

  /**
   * Gets a list of matching records.
   *
   * @throws IllegalArgumentException if the type does not exist.
   */
  List<Record> find(RecordQuery query);

  /**
   * Gets the number of matching records.
   *
   * @throws IllegalArgumentException if the type does not exist.
   */
  long count(RecordQuery query);

  /**
   * Deletes the given record. If it doesn't exist in storage, this has no effect.
   */
  void delete(Record record);

  /**
   * Closes the session, releasing any underlying resources.
   *
   * After calling this, all attempts to use this session or any records associated with it will
   * fail with an {@link IllegalStateException}.
   */
  @Override
  void close();
}
