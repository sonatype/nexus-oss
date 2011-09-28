package org.sonatype.sisu.locks;

import java.util.Collections;
import java.util.Map;

import org.sonatype.guice.bean.reflect.Weak;
import org.sonatype.sisu.locks.Locks.ResourceLock;

public abstract class AbstractSemaphoreResourceLock
    implements ResourceLock
{
    private static final int SHARED = 0;

    private static final int EXCLUSIVE = 1;

    private final Map<Thread, int[]> threadCounters = Collections.synchronizedMap( Weak.<Thread, int[]> keys() );

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

    protected abstract void acquire( int permits );

    protected abstract void release( int permits );

    protected abstract int availablePermits();
}
