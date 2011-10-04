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

    static Throwable[] errors;

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
        errors = new Throwable[ts.length];

        for ( int i = 0; i < ts.length; i++ )
        {
            final Locker locker = lookup( Locker.class );
            ts[i] = new Thread( locker );
            locker.setIndex( i );
        }

        running = true;

        for ( final Thread element : ts )
        {
            element.start();
        }

        Thread.sleep( 30000 );

        running = false;

        for ( final Thread element : ts )
        {
            element.join( 8000 );
        }

        boolean failed = false;
        for ( final Throwable e : errors )
        {
            if ( null != e )
            {
                e.printStackTrace();
                failed = true;
            }
        }
        assertFalse( failed );
    }

    @Named
    static class Locker
        implements Runnable
    {
        @Inject
        @Named( "${locks}" )
        private Locks locks;

        private int index;

        public void setIndex( final int index )
        {
            this.index = index;
        }

        public void run()
        {
            try
            {
                final ResourceLock lk = locks.getResourceLock( "TEST" );
                final Thread self = Thread.currentThread();

                while ( running )
                {
                    final double transition = Math.random();
                    if ( 0.0 <= transition && transition < 0.2 )
                    {
                        if ( sharedDepth[index] < 8 )
                        {
                            lk.lockShared( self );
                            sharedDepth[index]++;
                        }
                    }
                    else if ( 0.2 <= transition && transition < 0.3 )
                    {
                        if ( exclusiveDepth[index] < 8 )
                        {
                            lk.lockExclusive( self );
                            exclusiveDepth[index]++;
                        }
                    }
                    else if ( 0.3 <= transition && transition < 0.6 )
                    {
                        if ( exclusiveDepth[index] > 0 )
                        {
                            exclusiveDepth[index]--;
                            lk.unlockExclusive( self );
                        }
                        else
                        {
                            try
                            {
                                lk.unlockExclusive( self );
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
                            lk.unlockShared( self );
                        }
                        else
                        {
                            try
                            {
                                lk.unlockShared( self );
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
                    lk.unlockShared( self );
                    sharedDepth[index]--;
                }

                while ( exclusiveDepth[index] > 0 )
                {
                    lk.unlockExclusive( self );
                    exclusiveDepth[index]--;
                }
            }
            catch ( final Throwable e )
            {
                errors[index] = e;
            }
        }
    }
}
