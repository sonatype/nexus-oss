/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Before you continue using this class, stop and read carefully it's description. This "smart" lock is NOT an improved
 * implementation of Java's ReentrantReadWriteLock in any way. It does NOT implement the unsupported atomic
 * lock-upgrade. All this class allows is following pattern:
 * 
 * <pre>
 *   ...
 *   lock.lockShared();
 *   try {
 *     ...
 *     lock.lockExclusively();
 *     try {
 *       ...
 *     } finally {
 *       lock.unlock();
 *     }
 *     ...
 *   } finally {
 *     lock.unlock();
 *   }
 * </pre>
 * 
 * So, it is all about being able of "boxing" the locks.
 * <p>
 * Caveat No 1: Once you "upgrade" from shared to exclusive lock, you will possess exclusive lock as long as your last
 * {@link #unlock()} is invoked! While this is not totally correct or natural with "expectations", it does fit it's
 * purpose for use in Nexus.
 * <p>
 * Caveat No 2: The "upgrade" is not atomic, and it is actually not even meant to be atomic. If you take a peek at
 * implementation, by acquiring an exclusive lock, you actually give away all your shared locks, and just then tries to
 * acquire the exclusive lock! By releasing shared lock, you might actually let some other thread, that was waiting for
 * exclusive lock do something. Thus, only after acquiring exclusive lock you are assured that the path content state is
 * unchanged (ie. you should check for it's existence if you want to append to it).
 * 
 * @author cstamas
 */
class SimpleLockResource
    implements LockResource
{
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void lockShared()
    {
        if ( lock.getWriteHoldCount() > 0 )
        {
            lockExclusively();
        }
        else
        {
            lock.readLock().lock();
        }
    }

    @Override
    public void lockExclusively()
    {
        final int readLockCount = lock.getReadHoldCount();

        for ( int i = 0; i < readLockCount; i++ )
        {
            lock.readLock().unlock();
        }

        if ( readLockCount > 0 )
        {
            Thread.yield();
        }

        for ( int i = 0; i < readLockCount + 1; i++ )
        {
            lock.writeLock().lock();
        }
    }

    @Override
    public void unlock()
    {
        if ( lock.isWriteLockedByCurrentThread() )
        {
            lock.writeLock().unlock();
        }
        else
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasLocksHeld()
    {
        final int reads = lock.getReadHoldCount();

        final int writes = lock.getWriteHoldCount();

        return ( reads + writes ) != 0;
    }

    // ==

    /**
     * Mainly for debug purposes, see DefaultRepositoryItemUidTest UT how is this used to verify conditions.
     */
    public String toString()
    {
        return lock.toString();
    }
}