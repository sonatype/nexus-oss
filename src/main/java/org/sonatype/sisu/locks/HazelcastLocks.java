package org.sonatype.sisu.locks;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.InstanceDestroyedException;

@Named( "hazelcast" )
@Singleton
final class HazelcastLocks
    implements Locks
{
    @Inject
    HazelcastLocks( @Named( "hazelcast.config.xml" ) final String xml )
    {
        Hazelcast.init( new InMemoryXmlConfig( xml ) );
    }

    public SharedLock getSharedLock( final String name )
    {
        return new Impl( name );
    }

    private static final class Impl
        extends AbstractSemaphoreLock
    {
        private final ISemaphore sem;

        Impl( final String name )
        {
            sem = Hazelcast.getSemaphore( name );
            sem.release( Integer.MAX_VALUE );
        }

        @Override
        protected void acquire( final int permits )
        {
            while ( true )
            {
                try
                {
                    sem.acquireAttach( permits );
                }
                catch ( final InterruptedException e )
                {
                    Thread.currentThread().interrupt();
                }
                catch ( final InstanceDestroyedException e )
                {
                    throw new IllegalStateException( e );
                }
            }
        }

        @Override
        protected void release( final int permits )
        {
            sem.releaseDetach( permits );
        }
    }
}
