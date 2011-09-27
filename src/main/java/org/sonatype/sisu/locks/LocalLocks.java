package org.sonatype.sisu.locks;

import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

@Named( "local" )
@Singleton
public final class LocalLocks
    extends AbstractLocks
{
    @Override
    protected SharedLock create( String name )
    {
        return new Impl();
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

        @Override
        protected int availablePermits()
        {
            return sem.availablePermits();
        }
    }
}
