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
package org.sonatype.nexus.blobstore.api;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * <p>A generic storage bin for binary objects of all sizes.</p>
 *
 * <p>In general, most methods can throw BlobStoreException for conditions such as network connectivity problems, or
 * file IO issues, blob store misconfiguration, or internal corruption.</p>
 *
 * @since 3.0
 */
public interface BlobStore
{
  /**
   * Creates a new blob.
   *
   * Consider whether headers should turn into something more concrete, since these aren't supposed to be a dumping
   * ground for arbitrary metadata.
   *
   * Likely mandatory headers are:
   *
   * <ul>
   * <li>Identifying name for DR purposes (which isn't required to be strictly unique)</li>
   * <li>Audit information (e.g. the name of a principal)</li>
   * </ul>
   *
   * @throws BlobStoreException (or a subclass) if the input stream can't be read correctly, if mandatory headers are
   *                            missing,
   */
  Blob create(InputStream inputStream, Map<String, String> headers);

  @Nullable
  Blob get(BlobId blobId);

  /**
   * Removes a blob from the blob store.
   *
   * @return {@code true} if the blob has been deleted, {@code false} if no blob was found by that ID.
   * @throws BlobInUseException if the blob is currently in use by another thread
   */
  boolean delete(BlobId blobId);

  /**
   * Removes a blob from the blob store, disregarding any locking or concurrent access by other threads.
   *
   * @return {@code true} if the blob has been deleted, {@code false} if no blob was found by that ID.
   */
  boolean deleteHard(BlobId blobId);

  BlobStoreMetrics getMetrics();
}
