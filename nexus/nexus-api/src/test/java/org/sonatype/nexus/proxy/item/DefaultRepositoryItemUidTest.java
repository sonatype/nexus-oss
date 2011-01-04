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

import java.util.Random;

import junit.framework.TestCase;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultRepositoryItemUidTest
    extends TestCase
{
    private DummyRepositoryItemUidFactory factory;

    private Random random;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.factory = new DummyRepositoryItemUidFactory();

        this.random = new Random( System.currentTimeMillis() );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testLocking()
    {
        Repository repository = new DummyRepository( "dummy" );

        DefaultRepositoryItemUid uid = factory.createUid( repository, "/some/path.txt" );

        lockingSteps1( uid, null );

        verifyUidIsNotLocked( uid );
    }

    public void testMultiThreadedLocking()
        throws InterruptedException
    {
        Repository repository = new DummyRepository( "dummy" );

        DefaultRepositoryItemUid uid = factory.createUid( repository, "/some/path.txt" );

        Sleeper sleeper1 = new Sleeper()
        {
            public void sleep()
            {
                try
                {
                    Thread.sleep( Math.abs( random.nextLong() % 100 ) );
                }
                catch ( InterruptedException e )
                {
                    // noop
                }
            }
        };

        Sleeper sleeper2 = new Sleeper()
        {
            public void sleep()
            {
                try
                {
                    Thread.sleep( Math.abs( random.nextLong() % 75 ) );
                }
                catch ( InterruptedException e )
                {
                    // noop
                }
            }
        };

        LockingThreadSteps1 t1 = new LockingThreadSteps1( uid, 100, sleeper1 );
        LockingThreadSteps2 t2 = new LockingThreadSteps2( uid, 100, sleeper1 );
        LockingThreadSteps3 t3 = new LockingThreadSteps3( uid, 100, sleeper2 );
        LockingThreadSteps1 t4 = new LockingThreadSteps1( uid, 100, sleeper2 );
        LockingThreadSteps1 t5 = new LockingThreadSteps1( uid, 100, null );

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        Thread.sleep( 1000 );

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();

        // we have to have "clean" UID (not locked)
        verifyUidIsNotLocked( uid );
    }

    // verification steps

    public void verifyUidIsNotLocked( DefaultRepositoryItemUid uid )
    {
        verifyUidLockCounts( uid, 0, 0 );
    }

    public void verifyUidLockCounts( DefaultRepositoryItemUid uid, int wc, int rc )
    {
        // Warning: this trick is to present overall lock counts, and not owned by the current thread (before anyone
        // says "there is an API for this")

        String lockToString = uid.getContentLock().toString();

        assertTrue( "We expect " + wc + " write locks but have " + lockToString,
            lockToString.contains( "[Write locks = " + wc ) );
        assertTrue( "We expect " + rc + " read locks but have " + lockToString,
            lockToString.contains( ", Read locks = " + rc + "]" ) );
    }

    // test steps

    /**
     * Performs read, write, read locking on passed in UID, causing one upgrade to happen. Cleanup/unlock is done also.
     * This call returns "clean" (non locked UID).
     */
    public static void lockingSteps1( RepositoryItemUid uid, Sleeper sleeper )
    {
        // 1st lock is READ
        uid.lock( Action.read );
        sleep( sleeper );

        // 2nd lock is UPGRADE
        uid.lock( Action.create );
        sleep( sleeper );

        // 3rd lock is NOOP, since we already have WRITE lock but READ is requested (write is "stronger")
        uid.lock( Action.read );
        sleep( sleeper );

        // 3rd level unlock
        uid.unlock();
        sleep( sleeper );

        // 2nd level unlock
        uid.unlock();
        sleep( sleeper );

        // 1st level unlock
        uid.unlock();
        sleep( sleeper );
    }

    /**
     * Performs read locking on passed in UID, just to interfere with others wanting write. Cleanup/unlock is done also.
     * This call returns "clean" (non locked UID).
     */
    public static void lockingSteps2( RepositoryItemUid uid, Sleeper sleeper )
    {
        // 1st lock is READ
        uid.lock( Action.read );
        sleep( sleeper );

        // 1st level unlock
        uid.unlock();
        sleep( sleeper );
    }

    /**
     * Performs write locking on passed in UID, just to block others wanting anything. Cleanup/unlock is done also. This
     * call returns "clean" (non locked UID).
     */
    public static void lockingSteps3( RepositoryItemUid uid, Sleeper sleeper )
    {
        // 1st lock is WRITE
        uid.lock( Action.create );
        sleep( sleeper );

        // 1st level unlock
        uid.unlock();
        sleep( sleeper );
    }

    public static void sleep( Sleeper sleeper )
    {
        if ( sleeper != null )
        {
            sleeper.sleep();
        }
    }

    // threads

    public interface Sleeper
    {
        public void sleep();
    }

    /**
     * A thread using step1 count times, hence after this thread runs, no UID lock should exist.
     */
    public static abstract class LockingThreadSteps
        extends Thread
    {
        private final DefaultRepositoryItemUid uid;

        private final int count;

        private final Sleeper sleeper;

        public LockingThreadSteps( DefaultRepositoryItemUid uid, int count, Sleeper sleeper )
        {
            super();

            this.uid = uid;

            this.count = count;

            this.sleeper = sleeper;
        }

        public void run()
        {
            for ( int i = 0; i < count; i++ )
            {
                doIt( i, uid, sleeper );
            }
        }

        protected abstract void doIt( int count, DefaultRepositoryItemUid uid, Sleeper sleeper );
    }

    public static class LockingThreadSteps1
        extends LockingThreadSteps
    {
        public LockingThreadSteps1( DefaultRepositoryItemUid uid, int count, Sleeper sleeper )
        {
            super( uid, count, sleeper );
        }

        @Override
        protected void doIt( int count, DefaultRepositoryItemUid uid, Sleeper sleeper )
        {
            lockingSteps1( uid, sleeper );
        }
    }

    public static class LockingThreadSteps2
        extends LockingThreadSteps
    {
        public LockingThreadSteps2( DefaultRepositoryItemUid uid, int count, Sleeper sleeper )
        {
            super( uid, count, sleeper );
        }

        @Override
        protected void doIt( int count, DefaultRepositoryItemUid uid, Sleeper sleeper )
        {
            lockingSteps2( uid, sleeper );
        }
    }

    public static class LockingThreadSteps3
        extends LockingThreadSteps
    {
        public LockingThreadSteps3( DefaultRepositoryItemUid uid, int count, Sleeper sleeper )
        {
            super( uid, count, sleeper );
        }

        @Override
        protected void doIt( int count, DefaultRepositoryItemUid uid, Sleeper sleeper )
        {
            lockingSteps3( uid, sleeper );
        }
    }
}
