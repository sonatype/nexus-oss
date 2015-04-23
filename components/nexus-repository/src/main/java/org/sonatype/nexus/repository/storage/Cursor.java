/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A generic node cursor.
 *
 * @since 3.0
 */
public interface Cursor<T>
    extends AutoCloseable
{
  /**
   * Provides a chunk of nodes from cursor. If no more chunks, empty list is returned. It is not guaranteed that the
   * calls will happen using same {@link StorageTx}, so the cursor must keep internal state and probably implement
   * pagination on it's own, if needed.
   */
  @Nonnull
  List<T> next(StorageTx tx);

  /**
   * Closes the cursor, making the instance ineligible for further use.
   */
  void close();
}
