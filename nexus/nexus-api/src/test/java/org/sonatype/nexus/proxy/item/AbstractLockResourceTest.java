/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.access.Action;

public abstract class AbstractLockResourceTest
{
    private ExecutorService executor;

    @Before
    public void prepare()
    {
        executor = Executors.newFixedThreadPool( 5 );
    }

    protected abstract RepositoryItemUidLock getLockResource( final String name );

    /**
     * Testing shared access: we lock the resource using shared lock, spawn two shared locking threads and we expect
     * that all of those finish their work before we do.
     * 
     * @throws Exception
     */
    @Test
    public void sharedLockIsSharedAccess()
        throws Exception
    {
        final RepositoryItemUidLock slr = getLockResource( "foo" );

        final LockResourceRunnable r1 = new LockResourceRunnable( null, slr, Action.read );
        final LockResourceRunnable r2 = new LockResourceRunnable( null, slr, Action.read );

        slr.lock( Action.read );

        long unlockTs = -1;

        try
        {
            executor.execute( r1 );
            executor.execute( r2 );

            Thread.sleep( 100 );

            unlockTs = System.currentTimeMillis();
        }
        finally
        {
            slr.unlock();
        }

        executor.shutdown();
        executor.awaitTermination( 1000, TimeUnit.MILLISECONDS );

        assertThat( r1.getDoneTs(), lessThanOrEqualTo( unlockTs ) );
        assertThat( r2.getDoneTs(), lessThanOrEqualTo( unlockTs ) );
    }

    /**
     * Testing exclusive access: we lock the resource using exclusive lock, spawn two shared locking threads and we
     * expect that all of those finish their work after we do.
     * 
     * @throws Exception
     */
    @Test
    public void exclusiveLockIsNotSharedAccess()
        throws Exception
    {
        final RepositoryItemUidLock slr = getLockResource( "foo" );

        final LockResourceRunnable r1 = new LockResourceRunnable( null, slr, Action.read );
        final LockResourceRunnable r2 = new LockResourceRunnable( null, slr, Action.read );

        slr.lock( Action.create );

        long unlockTs = -1;

        try
        {
            executor.execute( r1 );
            executor.execute( r2 );

            Thread.sleep( 100 );

            unlockTs = System.currentTimeMillis();
        }
        finally
        {
            slr.unlock();
        }

        executor.shutdown();
        executor.awaitTermination( 1000, TimeUnit.MILLISECONDS );

        assertThat( r1.getDoneTs(), greaterThanOrEqualTo( unlockTs ) );
        assertThat( r2.getDoneTs(), greaterThanOrEqualTo( unlockTs ) );
    }

    /**
     * Testing lock downgrading: we lock the resource using shared lock, then using exclusive lock, spawn two shared
     * locking threads (that should block since we have exclusive lock), then we unlock (releasing the exclusive lock)
     * and we expect that all of those finish their work before we do.
     * <p>
     * Note: before downgradeable locks, this UT would fail, since after upgrade, exclusive lock would be released on
     * last unlock invocation. Having this test passing shows that downgrade does happen.
     * 
     * @throws Exception
     */
    @Test
    public void downgradeLockEnabledSharedAccess()
        throws Exception
    {
        final RepositoryItemUidLock slr = getLockResource( "foo" );

        final LockResourceRunnable r1 = new LockResourceRunnable( null, slr, Action.read );
        final LockResourceRunnable r2 = new LockResourceRunnable( null, slr, Action.read );

        slr.lock( Action.read );

        long unlockTs = -1;

        try
        {
            slr.lock( Action.create );

            try
            {
                executor.execute( r1 );
                executor.execute( r2 );
            }
            finally
            {
                slr.unlock();
            }

            Thread.sleep( 100 );

            unlockTs = System.currentTimeMillis();
        }
        finally
        {
            slr.unlock();
        }

        executor.shutdown();
        executor.awaitTermination( 1000, TimeUnit.MILLISECONDS );

        assertThat( r1.getDoneTs(), lessThanOrEqualTo( unlockTs ) );
        assertThat( r2.getDoneTs(), lessThanOrEqualTo( unlockTs ) );
    }

    /**
     * A simple static class that takes a lock and does something with it.
     * 
     * @author cstamas
     */
    public static class LockResourceRunnable
        implements Runnable
    {
        private final Runnable runnable;

        private final RepositoryItemUidLock uidLock;

        private final Action action;

        private long doneTs;

        public LockResourceRunnable( final Runnable runnable, final RepositoryItemUidLock lockResource,
                                     final Action action )
        {
            this.runnable = runnable;
            this.uidLock = lockResource;
            this.action = action;
            this.doneTs = -1;
        }

        public long getDoneTs()
        {
            return doneTs;
        }

        @Override
        public void run()
        {
            uidLock.lock( action );

            try
            {
                if ( runnable != null )
                {
                    runnable.run();
                }
            }
            finally
            {
                uidLock.unlock();
            }

            doneTs = System.currentTimeMillis();
        }
    }

}
