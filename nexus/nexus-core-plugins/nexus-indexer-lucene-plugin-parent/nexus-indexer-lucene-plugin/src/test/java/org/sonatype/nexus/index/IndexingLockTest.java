/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexingLockTest
{

    private ReadWriteLock locker;

    @Before
    public void setUp()
        throws Exception
    {
        locker = new ReentrantReadWriteLock();
    }

    @Test
    public void testIndexLocking()
    {
        beginIndex();
    }

    @Test
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
            Assert.assertFalse( hasLock );
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
            Assert.assertTrue( lock.tryLock() );
        }
        finally
        {
            lock.unlock();
        }
    }

}
