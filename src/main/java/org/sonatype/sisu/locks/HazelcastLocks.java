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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.inject.Nullable;

import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.InstanceDestroyedException;

/**
 * Distributed Hazelcast {@link Locks} implementation.
 */
@Named( "hazelcast" )
@Singleton
final class HazelcastLocks
    extends AbstractLocks
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

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

        try
        {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            final String type = getClass().getSimpleName();
            final ObjectName controller = ObjectName.getInstance( JMX_DOMAIN, properties( "type", type ) );
            final ObjectName query = ObjectName.getInstance( JMX_DOMAIN, properties( "type", type, "hash", "*" ) );
            if ( !server.isRegistered( controller ) )
            {
                server.registerMBean( new HazelcastLocksMBean( query ), controller );
            }
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected ResourceLock createResourceLock( final String name )
    {
        return new ResourceLockImpl( name );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ResourceLock} implemented on top of a Hazelcast {@link ISemaphore}.
     */
    private static final class ResourceLockImpl
        extends AbstractSemaphoreResourceLock
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final ISemaphore sem;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ResourceLockImpl( final String name )
        {
            sem = Hazelcast.getSemaphore( name );
        }

        // ----------------------------------------------------------------------
        // Semaphore methods
        // ----------------------------------------------------------------------

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
