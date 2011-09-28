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

import java.util.Collections;
import java.util.Map;

import org.sonatype.guice.bean.reflect.Weak;
import org.sonatype.sisu.locks.Locks.ResourceLock;

/**
 * {@link ResourceLock} implemented on top of an abstract semaphore.
 */
public abstract class AbstractSemaphoreResourceLock
    implements ResourceLock
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final int SHARED = 0;

    private static final int EXCLUSIVE = 1;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Thread, int[]> threadCounters = Collections.synchronizedMap( Weak.<Thread, int[]> keys() );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void lockShared()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters )
        {
            counters[SHARED]++;
        }
        else
        {
            acquire( 1 );
            threadCounters.put( self, new int[] { 1, 0 } );
        }
    }

    public final void lockExclusive()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters )
        {
            if ( counters[EXCLUSIVE] == 0 && counters[SHARED] > 0 )
            {
                /*
                 * Must drop shared lock before upgrading to exclusive lock
                 */
                release( 1 );
                threadCounters.remove( self );
                Thread.yield();
                acquire( Integer.MAX_VALUE );
                threadCounters.put( self, counters );
            }
            counters[EXCLUSIVE]++;
        }
        else
        {
            acquire( Integer.MAX_VALUE );
            threadCounters.put( self, new int[] { 0, 1 } );
        }
    }

    public final void unlockExclusive()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters && counters[EXCLUSIVE] > 0 )
        {
            if ( --counters[EXCLUSIVE] == 0 )
            {
                release( Integer.MAX_VALUE );
                threadCounters.remove( self );
                if ( counters[SHARED] > 0 )
                {
                    /*
                     * Downgrading from exclusive back to shared lock
                     */
                    Thread.yield();
                    acquire( 1 );
                    threadCounters.put( self, counters );
                }
            }
        }
        else
        {
            throw new IllegalStateException( self + " does not hold this resource" );
        }
    }

    public final void unlockShared()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters && counters[SHARED] > 0 )
        {
            if ( --counters[SHARED] == 0 && counters[EXCLUSIVE] == 0 )
            {
                release( 1 );
                threadCounters.remove( self );
            }
        }
        else
        {
            throw new IllegalStateException( self + " does not hold this resource" );
        }
    }

    public final boolean isExclusive()
    {
        return 0 == availablePermits();
    }

    public final int globalOwners()
    {
        final int n = availablePermits();
        return n > 0 ? Integer.MAX_VALUE - n : 1;
    }

    public final Thread[] localOwners()
    {
        return threadCounters.keySet().toArray( new Thread[0] );
    }

    public final int sharedCount( final Thread thread )
    {
        final int[] counters = threadCounters.get( thread );
        return null != counters ? counters[SHARED] : 0;
    }

    public final int exclusiveCount( final Thread thread )
    {
        final int[] counters = threadCounters.get( thread );
        return null != counters ? counters[EXCLUSIVE] : 0;
    }

    // ----------------------------------------------------------------------
    // Semaphore methods
    // ----------------------------------------------------------------------

    /**
     * @param permits Number of permits to acquire
     */
    protected abstract void acquire( int permits );

    /**
     * @param permits Number of permits to release
     */
    protected abstract void release( int permits );

    /**
     * @return Number of available permits
     */
    protected abstract int availablePermits();
}
