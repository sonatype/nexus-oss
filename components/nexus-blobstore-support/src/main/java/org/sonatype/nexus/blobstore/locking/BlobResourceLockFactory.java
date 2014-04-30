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

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.sisu.locks.ResourceLock;
import org.sonatype.sisu.locks.ResourceLockFactory;

import com.google.common.base.Preconditions;

/**
 * A {@link BlobLockFactory} backed by Nexus' standard {@link ResourceLockFactory} mechanism.
 *
 * @since 3.0
 */
public class BlobResourceLockFactory
    implements BlobLockFactory
{
  private ResourceLockFactory resourceLockFactory;

  public BlobResourceLockFactory(final ResourceLockFactory resourceLockFactory) {
    this.resourceLockFactory = resourceLockFactory;
  }

  @Override
  public BlobLock readLock(final BlobId blobId) {
    final ResourceLock resourceLock = resourceLockFactory.getResourceLock(blobId.toString());
    resourceLock.lockShared(Thread.currentThread());
    return new BlobResourceLock(resourceLock, false);
  }

  @Override
  public BlobLock exclusiveLock(final BlobId blobId) {
    final ResourceLock resourceLock = resourceLockFactory.getResourceLock(blobId.toString());
    resourceLock.lockExclusive(Thread.currentThread());
    return new BlobResourceLock(resourceLock, true);
  }

  @Nullable
  @Override
  public BlobLock tryExclusiveLock(final BlobId blobId) {
    final ResourceLock resourceLock = resourceLockFactory.getResourceLock(blobId.toString());
    if (resourceLock.tryExclusive(Thread.currentThread())) {
      return new BlobResourceLock(resourceLock, true);
    }
    return null;
  }

  private class BlobResourceLock
      implements BlobLock
  {
    private ResourceLock resourceLock;

    private boolean exclusive;

    private BlobResourceLock(final ResourceLock resourceLock, final boolean exclusive) {
      Preconditions.checkNotNull(resourceLock);
      this.resourceLock = resourceLock;
      this.exclusive = exclusive;
    }

    @Override
    public void close() {
      if (exclusive) {
        resourceLock.unlockExclusive(Thread.currentThread());
      }
      else {
        resourceLock.unlockShared(Thread.currentThread());
      }
    }
  }
}
