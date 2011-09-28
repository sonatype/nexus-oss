package org.sonatype.sisu.locks;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.inject.Nullable;

import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.InstanceDestroyedException;

@Named( "hazelcast" )
@Singleton
final class HazelcastLocks
    extends AbstractLocks
{
    @Inject
    HazelcastLocks( @Nullable @Named( "${hazelcast.config}" ) final File configFile )
    {
        if ( null != configFile && configFile.isFile() )
        {
            try
            {
                Hazelcast.init( new FileSystemXmlConfig( configFile ) );
            }
            catch ( final FileNotFoundException e )
            {
                throw new IllegalArgumentException( e.getMessage() );
            }
        }
        Hazelcast.getConfig().addSemaphoreConfig( new SemaphoreConfig( "default", Integer.MAX_VALUE ) );
    }

    @Override
    protected SharedLock create( String name )
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
        }

        @Override
        protected void acquire( final int permits )
        {
            while ( true )
            {
                try
                {
                    sem.acquireAttach( permits );
                    return;
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

        @Override
        protected int availablePermits()
        {
            return sem.availablePermits();
        }
    }
}
