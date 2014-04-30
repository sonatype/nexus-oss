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
package org.sonatype.nexus.blobstore.locking;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobInUseException;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreMetrics;

/**
 * A {@link BlobStore} wrapper that ensures locks are obtained before delegating to the inner blob store.
 *
 * @since 3.0
 */
public class LockingBlobStore
    extends WrappingBlobStoreSupport
{
  private BlobLockFactory lockProvider;

  public LockingBlobStore(final BlobStore wrappedBlobStore,
                          final BlobLockFactory lockProvider)
  {
    super(wrappedBlobStore);
    this.lockProvider = lockProvider;
  }

  /**
   * Note: creation is not locked, so internal cleanup processes that might be looking at half-created blobs, should
   * definitely defer to the same {@link BlobLockFactory} as this LockingBlobStore.
   */
  @Override
  public Blob create(final InputStream inputStream, final Map<String, String> headers) {
    return super.create(inputStream, headers);
  }

  @Nullable
  @Override
  public Blob get(final BlobId blobId) {
    try (BlobLock lock = lockProvider.readLock(blobId)) {
      final Blob blob = super.get(blobId);

      return new Blob()
      {
        @Override
        public BlobId getId() {
          return blobId;
        }

        @Override
        public Map<String, String> getHeaders() {
          try (BlobLock lock = lockProvider.readLock(blobId)) {
            return blob.getHeaders();
          }
        }

        /**
         * Usage of the input stream isn't locked, only obtaining it.
         */
        @Override
        public InputStream getInputStream() {
          BlobLock lock = lockProvider.readLock(blobId);
          return new LockHoldingInputStream(blob.getInputStream(), lock);
        }

        @Override
        public BlobMetrics getMetrics() {
          try (BlobLock lock = lockProvider.readLock(blobId)) {
            return blob.getMetrics();
          }
        }
      };
    }
  }

  @Override
  public boolean delete(final BlobId blobId) {
    BlobLock lock = lockProvider.tryExclusiveLock(blobId);
    if (lock == null) {
      throw new BlobInUseException("Blobs can't be deleted normally while they are in use.");
    }
    return super.delete(blobId);
  }

  @Override
  public boolean deleteHard(final BlobId blobId)
  {
    // Ignore all locks, hard deletion bypasses them.
    // TODO: Consider whether we ought to invalidate existing locks.
    return super.deleteHard(blobId);
  }

  @Override
  public BlobStoreMetrics getMetrics() {
    return super.getMetrics();
  }
}
