package org.sonatype.sisu.locks;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.Weak;

@Named( "local" )
@Singleton
public final class LocalSemaphores
    implements Semaphores
{
    private final ConcurrentMap<String, Impl> semaphores = Weak.concurrentValues();

    public Sem get( final String name )
    {
        Impl sem = semaphores.get( name );
        if ( null == sem )
        {
            final Impl oldSem = semaphores.putIfAbsent( name, sem = new Impl() );
            if ( null != oldSem )
            {
                return oldSem;
            }
        }
        return sem;
    }

    public static final class Impl
        extends AbstractSem
    {
        private final Semaphore sem = new Semaphore( Integer.MAX_VALUE );

        @Override
        protected void acquire( int permits )
        {
            sem.acquireUninterruptibly( permits );
        }

        @Override
        protected void release( int permits )
        {
            sem.release( permits );
        }
    }
}
