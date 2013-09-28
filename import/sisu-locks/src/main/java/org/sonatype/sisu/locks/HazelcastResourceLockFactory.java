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

import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.inject.Nullable;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.InstanceDestroyedException;

/**
 * Distributed Hazelcast {@link ResourceLockFactory} implementation.
 */
@Named( "hazelcast" )
@Singleton
final class HazelcastResourceLockFactory
    extends AbstractResourceLockFactory
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final HazelcastInstance instance;

    private ObjectName jmxQuery;

    private ObjectName jmxMaster;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    HazelcastResourceLockFactory( @Nullable @Named( "hazelcast.config" ) final File configFile )
    {
        super( true );

        instance = Hazelcast.newHazelcastInstance( getHazelcastConfig( configFile ) );

        try
        {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            jmxMaster = ObjectName.getInstance( JMX_DOMAIN, "type", category() );
            jmxQuery = ObjectName.getInstance( JMX_DOMAIN, properties( "type", category(), "hash", "*" ) );
            if ( !server.isRegistered( jmxMaster ) )
            {
                server.registerMBean( new HazelcastResourceLockMBean( instance, jmxQuery ), jmxMaster );
            }
        }
        catch ( final Exception e )
        {
            Logs.warn( "Problem registering master LocksMBean for: <>", this, e );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void shutdown()
    {
        boolean lastMember = false;
        try
        {
            super.shutdown();
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            lastMember = server.queryNames( jmxQuery, null ).isEmpty();
            if ( lastMember )
            {
                try
                {
                    server.unregisterMBean( jmxMaster );
                }
                catch ( final Exception e )
                {
                    Logs.warn( "Problem unregistering master LocksMBean for: <>", this, e );
                }
            }
        }
        finally
        {
            if ( lastMember )
            {
                Hazelcast.shutdownAll();
            }
            else
            {
                instance.getLifecycleService().shutdown();
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected String category()
    {
        return HazelcastResourceLock.class.getSimpleName() + 's';
    }

    @Override
    protected ResourceLock createResourceLock( final String name )
    {
        return new HazelcastResourceLock( instance.getSemaphore( name ) );
    }

    /**
     * @return Hazelcast configuration; ensures semaphores start with {@link Integer#MAX_VALUE} permits by default
     */
    private static Config getHazelcastConfig( final File configFile )
    {
        final Config config;
        if ( null != configFile && configFile.isFile() )
        {
            try
            {
                config = new FileSystemXmlConfig( configFile );
            }
            catch ( final FileNotFoundException e )
            {
                throw new IllegalArgumentException( e.getMessage() );
            }
        }
        else
        {
            config = new XmlConfigBuilder().build();
        }

        config.getSemaphoreConfig( "default" ).setInitialPermits( Integer.MAX_VALUE );
        config.setClassLoader( Hazelcast.class.getClassLoader() );

        return config;
    }
}

/**
 * {@link ResourceLock} implemented on top of a Hazelcast {@link ISemaphore}.
 */
final class HazelcastResourceLock
    extends AbstractSemaphoreResourceLock
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ISemaphore sem;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    HazelcastResourceLock( final ISemaphore sem )
    {
        this.sem = sem;
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
