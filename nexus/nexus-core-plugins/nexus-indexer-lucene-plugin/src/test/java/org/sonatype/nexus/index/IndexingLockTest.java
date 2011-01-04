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
package org.sonatype.nexus.index;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import junit.framework.TestCase;

public class IndexingLockTest
    extends TestCase
{

    private ReadWriteLock locker;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        locker = new ReentrantReadWriteLock();
    }

    public void testIndexLocking()
    {
        beginIndex();
    }

    public void testThreadIndexLocking() throws InterruptedException
    {
        new Thread( new Runnable()
        {

            public void run()
            {
                Lock lock = locker.writeLock();
                lock.lock();

                Thread.yield();

                try
                {
                    Thread.sleep( 3000 );
                }
                catch ( InterruptedException e )
                {
                    // no problem
                }
                finally
                {
                    lock.unlock();
                }
            }
        } ).start();

        Thread.yield();
        Thread.sleep( 100 );
        Thread.yield();

        Lock lock = locker.readLock();
        boolean hasLock = true;
        try
        {
            hasLock = lock.tryLock();
            assertFalse( hasLock );
        }
        finally
        {
            if ( hasLock )
                lock.unlock();
        }
    }

    private void beginIndex()
    {
        Lock lock = locker.writeLock();
        lock.lock();
        try
        {
            downloadRemoteIndex();
        }
        finally
        {
            lock.unlock();
        }
    }

    private void downloadRemoteIndex()
    {
        Lock lock = locker.readLock();
        try
        {
            assertTrue( lock.tryLock() );
        }
        finally
        {
            lock.unlock();
        }
    }

}
