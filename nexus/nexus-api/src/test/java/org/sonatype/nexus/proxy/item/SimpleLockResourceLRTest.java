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

        Thread t1 = new Thread( new Locker( repoLock ) );
        Thread t2 = new Thread( new Locker( repoLock ) );
        Thread t3 = new Thread( new Locker( repoLock ) );
        Thread t4 = new Thread( new Locker( repoLock ) );
        Thread t5 = new Thread( new Locker( repoLock ) );

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
    }

    // ==

    public static class Locker
        implements Runnable
    {
        private static int sharedResource = 0;

        private static int getSharedResource()
        {
            // logger.info( "Thread {} reads {}", Thread.currentThread().getName(), sharedResource );

            return sharedResource;
        }

        private static void setSharedResource( int val )
        {
            // logger.info( "Thread {} sets {} to {}", new Object[] { Thread.currentThread().getName(), sharedResource,
            // val } );

            sharedResource = val;
        }

        private final LockResource lock;

        public Locker( final LockResource lock )
        {
            this.lock = lock;
        }

        @Override
        public void run()
        {
            try
            {
                for ( int i = 0; i < 100; i++ )
                {
                    lock.lockShared();

                    int sharedBeforeChange = getSharedResource();

                    int shared = -1;

                    try
                    {
                        // do something
                        Thread.sleep( rnd.nextInt( 50 ) );

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

                        // this test uses the following fact: Write lock, once acquired is held all the way, even we
                        // stepped out of the "boxed" access (unlock above). See SmartLock class, "upgrade"
                        // happens, but
                        // not downgrade. Simply, if you have read locks in a moment you acquire write lock, they will
                        // be "converted"
                        // to write locks (to maintain boxing depth and number of unlock() invocations).

                        // So, the code below STILL EXECUTES IN EXCLUSIVE lock!
                        int sharedAfterChange = getSharedResource();

                        // Assertion (note, it's STRICTLY LESS-THEN):
                        // sharedBeforeChange < shared = sharedAfterChange
                        if ( !( ( sharedBeforeChange < shared ) && ( shared == sharedAfterChange ) ) )
                        {
                            throw new IllegalStateException( Thread.currentThread().getName() + " before:"
                                + sharedBeforeChange + " shared:" + shared + " after:" + sharedAfterChange );
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
            catch ( InterruptedException e )
            {
                throw new IllegalStateException( e );
            }
        }
    }

}