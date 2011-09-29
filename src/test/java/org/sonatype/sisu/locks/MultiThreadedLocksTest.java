/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.inject.Parameters;
import org.sonatype.sisu.locks.Locks.ResourceLock;

import com.google.inject.Provides;

public class MultiThreadedLocksTest
    extends InjectedTestCase
{
    static volatile boolean running;

    static Thread[] ts;

    static int[] sharedDepth;

    static int[] exclusiveDepth;

    @Provides
    @Parameters
    Properties systemProperties()
    {
        return System.getProperties();
    }

    public void testDefaultLocks()
        throws InterruptedException
    {
        System.setProperty( "locks", "default" );

        launchThreads();
    }

    public void testHazelcastLocks()
        throws InterruptedException
    {
        System.setProperty( "locks", "hazelcast" );

        launchThreads();
    }

    private void launchThreads()
        throws InterruptedException
    {
        ts = new Thread[128];

        sharedDepth = new int[ts.length];
        exclusiveDepth = new int[ts.length];

        for ( int i = 0; i < ts.length; i++ )
        {
            final Locker locker = lookup( Locker.class );
            ts[i] = new Thread( locker );
            locker.setIndex( i );
        }

        running = true;

        for ( int i = 0; i < ts.length; i++ )
        {
            ts[i].start();
        }

        Thread.sleep( 30000 );

        running = false;

        for ( int i = 0; i < ts.length; i++ )
        {
            ts[i].join();
        }
    }

    @Named
    static class Locker
        implements Runnable
    {
        @Inject
        @Named( "${locks}" )
        private Locks locks;

        private int index;

        public void setIndex( int index )
        {
            this.index = index;
        }

        public void run()
        {
            final ResourceLock lk = locks.getResourceLock( "TEST" );

            while ( running )
            {
                final double transition = Math.random();
                if ( 0.0 <= transition && transition < 0.2 )
                {
                    if ( sharedDepth[index] < 8 )
                    {
                        lk.lockShared();
                        sharedDepth[index]++;
                    }
                }
                else if ( 0.2 <= transition && transition < 0.3 )
                {
                    if ( exclusiveDepth[index] < 8 )
                    {
                        lk.lockExclusive();
                        exclusiveDepth[index]++;
                    }
                }
                else if ( 0.3 <= transition && transition < 0.6 )
                {
                    if ( exclusiveDepth[index] > 0 )
                    {
                        exclusiveDepth[index]--;
                        lk.unlockExclusive();
                    }
                    else
                    {
                        try
                        {
                            lk.unlockExclusive();
                            fail( "Expected IllegalStateException" );
                        }
                        catch ( final IllegalStateException e )
                        {
                            // expected
                        }
                    }
                }
                else
                {
                    if ( sharedDepth[index] > 0 )
                    {
                        sharedDepth[index]--;
                        lk.unlockShared();
                    }
                    else
                    {
                        try
                        {
                            lk.unlockShared();
                            fail( "Expected IllegalStateException" );
                        }
                        catch ( final IllegalStateException e )
                        {
                            // expected
                        }
                    }
                }
            }

            while ( sharedDepth[index] > 0 )
            {
                lk.unlockShared();
                sharedDepth[index]--;
            }

            while ( exclusiveDepth[index] > 0 )
            {
                lk.unlockExclusive();
                exclusiveDepth[index]--;
            }
        }
    }
}
