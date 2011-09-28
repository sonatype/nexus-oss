package org.sonatype.sisu.locks;

import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public final class DefaultLocks
    extends AbstractLocks
{
    @Override
    protected ResourceLock create( String name )
    {
        return new ResourceLockImpl();
    }

    public static final class ResourceLockImpl
        extends AbstractSemaphoreResourceLock
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
