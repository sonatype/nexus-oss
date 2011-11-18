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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import org.junit.Test;

public class SimpleLockResourceLRTest
{
    private static Random rnd = new Random( System.currentTimeMillis() );

    @Test
    public void testSimple()
        throws InterruptedException
    {
        LockResource repoLock = new SimpleLockResource();

        Locker t1 = createThread( repoLock, "t1" );
        Locker t2 = createThread( repoLock, "t2" );
        Locker t3 = createThread( repoLock, "t3" );
        Locker t4 = createThread( repoLock, "t4" );
        Locker t5 = createThread( repoLock, "t5" );

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();

        assertThreadNotFailed( t1 );
        assertThreadNotFailed( t2 );
        assertThreadNotFailed( t3 );
        assertThreadNotFailed( t4 );
        assertThreadNotFailed( t5 );
    }

    protected Locker createThread( final LockResource repoLock, final String name )
    {
        final Locker locker = new Locker( repoLock );
        locker.setName( name );
        return locker;
    }

    protected void assertThreadNotFailed( final Locker t )
    {
        final Throwable throwable = t.getThrowable();

        if ( throwable != null )
        {
            assertThat( t.getName() + " failed with: " + throwable.getMessage(), false );
        }
    }

    // ==

    private static int sharedResource = 0;

    protected static int getSharedResource()
    {
        return sharedResource;
    }

    protected static void setSharedResource( int val )
    {
        if ( val != ( sharedResource + 1 ) )
        {
            throw new IllegalArgumentException( Thread.currentThread().getName()
                + " thread would overwrite shared resource with a stale state!" );
        }

        sharedResource = val;
    }

    // ==

    public static class Locker
        extends Thread
        implements UncaughtExceptionHandler
    {
        private final LockResource lock;

        public Locker( final LockResource lock )
        {
            this.lock = lock;
            setUncaughtExceptionHandler( this );
        }

        @Override
        public void run()
        {
            try
            {
                for ( int i = 0; i < 100; i++ )
                {
                    int sharedBeforeChange = getSharedResource();
                    int shared = -1;

                    lock.lockShared();

                    try
                    {
                        // do something needing write
                        lock.lockExclusively();

                        try
                        {
                            shared = getSharedResource() + 1;

                            // do something
                            Thread.sleep( rnd.nextInt( 50 ) );

                            setSharedResource( shared );

                            lock.lockShared();

                            try
                            {
                                // do something
                                Thread.sleep( rnd.nextInt( 50 ) );
                            }
                            finally
                            {
                                lock.unlock();
                            }
                        }
                        finally
                        {
                            lock.unlock();
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }

                    // kick other threads
                    Thread.yield();

                    // do something
                    Thread.sleep( rnd.nextInt( 50 ) );

                    // This code executes OUTSIDE of critical region!
                    int sharedAfterChange = getSharedResource();

                    // Assertion (note, it's STRICTLY LESS-THEN):
                    // sharedBeforeChange < shared = sharedAfterChange
                    if ( !( ( sharedBeforeChange < shared ) && ( shared <= sharedAfterChange ) ) )
                    {
                        throw new IllegalStateException( Thread.currentThread().getName() + " before:"
                            + sharedBeforeChange + " shared:" + shared + " after:" + sharedAfterChange );
                    }
                }
            }
            catch ( InterruptedException e )
            {
                throw new IllegalStateException( e );
            }
        }

        private Throwable throwable;

        @Override
        public void uncaughtException( Thread t, Throwable e )
        {
            this.throwable = e;
        }

        public Throwable getThrowable()
        {
            return this.throwable;
        }
    }

}