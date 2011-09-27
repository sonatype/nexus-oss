package org.sonatype.sisu.locks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.sonatype.sisu.locks.Locks.SharedLock;

public abstract class AbstractSemaphoreLock
    implements SharedLock
{
    private final Map<Thread, int[]> threadCounters = Collections.synchronizedMap( new WeakHashMap<Thread, int[]>() );

    public final void lockShared()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters )
        {
            counters[0]++;
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
            if ( counters[1] == 0 && counters[0] > 0 )
            {
                release( 1 );
                threadCounters.remove( self );
                Thread.yield();
                acquire( Integer.MAX_VALUE );
                threadCounters.put( self, counters );
            }
            counters[1]++;
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
        if ( null != counters && counters[1] > 0 )
        {
            if ( --counters[1] == 0 )
            {
                release( Integer.MAX_VALUE );
                threadCounters.remove( self );
                if ( counters[0] > 0 )
                {
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
        if ( null != counters && counters[0] > 0 )
        {
            if ( --counters[0] == 0 && counters[1] == 0 )
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

    public int sharedLockCount( final Thread thread )
    {
        final int[] counters = threadCounters.get( thread );
        return null != counters ? counters[0] : 0;
    }

    public int exclusiveLockCount( final Thread thread )
    {
        final int[] counters = threadCounters.get( thread );
        return null != counters ? counters[1] : 0;
    }

    public Collection<Thread> owners()
    {
        return new ArrayList<Thread>( threadCounters.keySet() );
    }

    protected abstract void acquire( int permits );

    protected abstract void release( int permits );
}
