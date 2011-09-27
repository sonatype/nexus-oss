package org.sonatype.sisu.locks;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.sonatype.sisu.locks.Semaphores.Sem;

public abstract class AbstractSem
    implements Sem
{
    private final Map<Thread, int[]> threadCounters = Collections.synchronizedMap( new WeakHashMap<Thread, int[]>() );

    public final void acquireShared()
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

    public final void acquireExclusive()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters )
        {
            if ( counters[1] == 0 && counters[0] > 0 )
            {
                release( 1 );
                Thread.yield();
                acquire( Integer.MAX_VALUE );
            }
            counters[1]++;
        }
        else
        {
            acquire( Integer.MAX_VALUE );
            threadCounters.put( self, new int[] { 0, 1 } );
        }
    }

    public final void releaseExclusive()
    {
        final Thread self = Thread.currentThread();
        final int[] counters = threadCounters.get( self );
        if ( null != counters && counters[1] > 0 )
        {
            if ( --counters[1] == 0 )
            {
                release( Integer.MAX_VALUE );
                if ( counters[0] > 0 )
                {
                    Thread.yield();
                    acquire( 1 );
                }
                else
                {
                    threadCounters.remove( self );
                }
            }
        }
        else
        {
            throw new IllegalStateException( self + " does not hold this resource" );
        }
    }

    public final void releaseShared()
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

    public final int[] getHoldCounts()
    {
        return threadCounters.get( Thread.currentThread() );
    }

    @Override
    public String toString()
    {
        final Thread self = Thread.currentThread();
        int[] counters = threadCounters.get( self );
        if ( null == counters )
        {
            counters = new int[2];
        }
        return super.toString() + "[Write locks = " + counters[1] + ", Read locks = " + counters[0] + "]";
    }

    protected abstract void acquire( int permits );

    protected abstract void release( int permits );
}
