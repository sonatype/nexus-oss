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

import java.util.Set;

/**
 * A storage object containing a set of key-value pairs.
 *
 * @since 3.0
 */
public interface Record
{
  /**
   * Gets the type of this record.
   */
  RecordType getType();

  /**
   * Tells whether this record has ever been {@link #save()}d. If {@code true}, it is guaranteed to have an id
   * and version.
   */
  boolean isPersistent();

  /**
   * Gets the id of this record.
   *
   * @throws IllegalStateException if the record is not persistent.
   * @see #isPersistent()
   */
  RecordId getId();

  /**
   * Gets the version of this record.
   *
   * @throws IllegalStateException if the record is not persistent.
   * @see #isPersistent()
   */
  RecordVersion getVersion();

  /**
   * Tells whether the record has a value defined for the given field.
   */
  boolean has(String key);

  /**
   * Gets the value of a field.
   *
   * @throws NullPointerException if the value is undefined. If uncertain, callers should use {@link #has(String)} to
   * determine if the value is defined first.
   */
  <T> T get(String key);

  /**
   * Gets the value of a {@code Record} field.
   *
   * @throws NullPointerException if the value is undefined. If uncertain, callers should use {@link #has(String)} to
   * determine if the value is defined first.
   */
  Record getRecord(String key);

  /**
   * Sets the value of a field.
   */
  Record set(String key, Object value);

  /**
   * Removes a field by key. If the field does not exist, this has no effect.
   */
  void remove(String key);

  /**
   * Gets the list of field names.
   */
  Set<String> keySet();

  /**
   * Persists the current state of the record to the store.
   *
   * If this is the first time the record has been saved, it will be assigned a permanent
   * id and an initial version. If the record already exists, the version will be incremented.
   *
   * @see #getId()
   * @see #getVersion()
   * @see #isPersistent()
   */
  Record save();
}
