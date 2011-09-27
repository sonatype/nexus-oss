package org.sonatype.sisu.locks;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.Weak;

@Named( "local" )
@Singleton
public final class LocalLocks
    implements Locks
{
    private final ConcurrentMap<String, Impl> sharedLocks = Weak.concurrentValues();

    public SharedLock getSharedLock( final String name )
    {
        Impl sem = sharedLocks.get( name );
        if ( null == sem )
        {
            final Impl oldSem = sharedLocks.putIfAbsent( name, sem = new Impl() );
            if ( null != oldSem )
            {
                return oldSem;
            }
        }
        return sem;
    }

    public static final class Impl
        extends AbstractSemaphoreLock
    {
        private final Semaphore sem = new Semaphore( Integer.MAX_VALUE );

        @Override
        protected void acquire( final int permits )
        {
            sem.acquireUninterruptibly( permits );
        }

        @Override
        protected void release( final int permits )
        {
            sem.release( permits );
        }
    }
}
