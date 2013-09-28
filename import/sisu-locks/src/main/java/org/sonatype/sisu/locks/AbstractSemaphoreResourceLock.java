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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Thread, int[]> map = new ConcurrentHashMap<Thread, int[]>( 16, 0.75f, 1 );

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void lockShared( final Thread thread )
    {
        int[] counters = map.get( thread );
        if ( null == counters )
        {
            counters = new int[] { 0, 0 };
            map.put( thread, counters );
            acquire( 1 );
        }
        counters[SHARED]++;
    }

    public final void lockExclusive( final Thread thread )
    {
        int[] counters = map.get( thread );
        if ( null == counters )
        {
            counters = new int[] { 0, 0 };
            map.put( thread, counters );
            acquire( Integer.MAX_VALUE );
        }
        else if ( counters[EXCLUSIVE] == 0 )
        {
            final int shared = counters[SHARED];
            /*
             * Must drop shared lock before upgrading to exclusive lock
             */
            release( 1 );
            counters[SHARED] = 0;
            acquire( Integer.MAX_VALUE );
            counters[SHARED] = shared;
        }
        counters[EXCLUSIVE]++;
    }

    public final void unlockExclusive( final Thread thread )
    {
        final int[] counters = map.get( thread );
        if ( null != counters && counters[EXCLUSIVE] > 0 )
        {
            if ( --counters[EXCLUSIVE] == 0 )
            {
                release( Integer.MAX_VALUE );
                final int shared = counters[SHARED];
                if ( shared > 0 )
                {
                    /*
                     * Down-grading from exclusive back to shared lock
                     */
                    counters[SHARED] = 0;
                    acquire( 1 );
                    counters[SHARED] = shared;
                }
                else
                {
                    map.remove( thread );
                }
            }
        }
        else
        {
            throw new IllegalStateException( thread + " does not hold this resource" );
        }
    }

    public final void unlockShared( final Thread thread )
    {
        final int[] counters = map.get( thread );
        if ( null != counters && counters[SHARED] > 0 )
        {
            if ( --counters[SHARED] == 0 && counters[EXCLUSIVE] == 0 )
            {
                release( 1 );
                map.remove( thread );
            }
        }
        else
        {
            throw new IllegalStateException( thread + " does not hold this resource" );
        }
    }

    public final Thread[] getOwners()
    {
        final List<Thread> owners = new ArrayList<Thread>();
        for ( final Entry<Thread, int[]> e : map.entrySet() )
        {
            final int[] counters = e.getValue();
            if ( counters[SHARED] > 0 || counters[EXCLUSIVE] > 0 )
            {
                owners.add( e.getKey() );
            }
        }
        return owners.toArray( new Thread[owners.size()] );
    }

    public final Thread[] getWaiters()
    {
        final List<Thread> waiters = new ArrayList<Thread>();
        for ( final Entry<Thread, int[]> e : map.entrySet() )
        {
            final int[] counters = e.getValue();
            if ( counters[SHARED] == 0 && counters[EXCLUSIVE] == 0 )
            {
                waiters.add( e.getKey() );
            }
        }
        return waiters.toArray( new Thread[waiters.size()] );
    }

    public final int getSharedCount( final Thread thread )
    {
        final int[] counters = map.get( thread );
        return null != counters ? counters[SHARED] : 0;
    }

    public final int getExclusiveCount( final Thread thread )
    {
        final int[] counters = map.get( thread );
        return null != counters ? counters[EXCLUSIVE] : 0;
    }

    @Override
    public String toString()
    {
        final int permits = availablePermits();
        final int owners = permits > 0 ? Integer.MAX_VALUE - permits : 1;
        return "[Owners = " + owners + ", Exclusive = " + ( permits == 0 ) + "]";
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
