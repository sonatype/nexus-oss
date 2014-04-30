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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A straightforward implementation of {@link BlobLockFactory} that delegates to an in-memory collection of {@link
 * ReadWriteLock}.
 *
 * A central concern is managing the pool of locks, which are dynamically created as they are required for various
 * blobIds.
 *
 * @since 3.0
 */
public class SimpleLockFactory
    implements BlobLockFactory
{
  private static final Logger logger = LoggerFactory.getLogger(SimpleLockFactory.class);

  private final SetMultimap<BlobId, SimpleBlobLock> locks = HashMultimap.create();

  @Override
  public synchronized BlobLock readLock(final BlobId blobId) {
    Preconditions.checkNotNull(blobId);

    ReadWriteLock readWriteLock = findLock(blobId);
    final Lock lock = readWriteLock.readLock();
    lock.lock();

    return makeAndStoreNewBlobLock(blobId, readWriteLock, lock);
  }


  @Override
  public synchronized BlobLock exclusiveLock(final BlobId blobId) {
    Preconditions.checkNotNull(blobId);

    ReadWriteLock readWriteLock = findLock(blobId);
    final Lock lock = readWriteLock.writeLock();
    lock.lock();

    return makeAndStoreNewBlobLock(blobId, readWriteLock, lock);
  }

  @Nullable
  @Override
  public synchronized BlobLock tryExclusiveLock(final BlobId blobId) {
    Preconditions.checkNotNull(blobId);

    ReadWriteLock readWriteLock = findLock(blobId);
    final Lock lock = readWriteLock.writeLock();

    if (!lock.tryLock()) {
      return null;
    }

    return makeAndStoreNewBlobLock(blobId, readWriteLock, lock);
  }

  private SimpleBlobLock makeAndStoreNewBlobLock(final BlobId blobId, final ReadWriteLock readWriteLock,
                                                 final Lock lock)
  {
    final SimpleBlobLock simpleBlobLock = new SimpleBlobLock(blobId, readWriteLock, lock);
    locks.put(blobId, simpleBlobLock);
    return simpleBlobLock;
  }

  private synchronized void release(SimpleBlobLock blobLock) {
    final Set<SimpleBlobLock> simpleBlobLocks = locks.get(blobLock.getBlobId());
    simpleBlobLocks.remove(blobLock);
  }

  private ReadWriteLock findLock(final BlobId blobId) {
    final Set<SimpleBlobLock> simpleBlobLocks = locks.get(blobId);

    if (simpleBlobLocks != null && !simpleBlobLocks.isEmpty()) {
      return simpleBlobLocks.iterator().next().getReadWriteLock();
    }

    return new ReentrantReadWriteLock();
  }

  private class SimpleBlobLock
      implements BlobLock
  {
    private BlobId blobId;

    private ReadWriteLock readWriteLock;

    private Lock lock;

    private SimpleBlobLock(final BlobId blobId, final ReadWriteLock readWriteLock, final Lock lock) {
      this.blobId = blobId;
      this.readWriteLock = readWriteLock;
      this.lock = lock;
    }

    public BlobId getBlobId() {
      return blobId;
    }

    public ReadWriteLock getReadWriteLock() {
      return readWriteLock;
    }

    @Override
    public void close() {
      lock.unlock();
      release(this);
      this.blobId = null;
    }

    @Override
    protected void finalize() throws Throwable {
      if (blobId != null) {
        close();
        logger.warn("Blob lock leakage detected.");
      }
      super.finalize();
    }
  }

}
