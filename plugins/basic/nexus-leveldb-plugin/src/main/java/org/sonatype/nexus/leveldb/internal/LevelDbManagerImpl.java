/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.leveldb.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.leveldb.LevelDbManager;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

/**
 * Implementation of {@link LevelDbManager}.
 * 
 * @author cstamas
 */
@Singleton
@Named
public class LevelDbManagerImpl
    extends AbstractLoggingComponent
    implements LevelDbManager
{
    private final ApplicationConfiguration applicationConfiguration;

    private final Map<String, DB> managedDatabases;

    @Inject
    public LevelDbManagerImpl( final EventBus eventBus, final ApplicationConfiguration applicationConfiguration )
    {
        this.applicationConfiguration = checkNotNull( applicationConfiguration );
        this.managedDatabases = Maps.newHashMap();
        eventBus.register( this );
    }

    @Override
    public DB openManaged( final String dbKey, final Options options )
        throws IOException
    {
        checkNotNull( dbKey, "dbKey is null" );
        checkNotNull( options, "options is null" );
        final File location = new File( applicationConfiguration.getWorkingDirectory( "leveldb" ), dbKey );
        return openManaged( dbKey, location, options );
    }

    @Override
    public DB openManaged( final String dbKey, final File file, final Options options )
        throws IOException, IllegalArgumentException
    {
        checkNotNull( dbKey, "dbKey is null" );
        checkArgument( !file.exists() || file.isDirectory() );
        checkNotNull( options, "options is null" );
        synchronized ( managedDatabases )
        {
            if ( managedDatabases.containsKey( dbKey ) )
            {
                throw new IllegalArgumentException( "Database keyed \"" + dbKey + "\" already created and is managed!" );
            }
            final DB result = create( file, options );
            managedDatabases.put( dbKey, result );
            return result;
        }
    }

    @Override
    public DB getManaged( final String dbKey )
    {
        checkNotNull( dbKey, "dbKey is null" );
        synchronized ( managedDatabases )
        {
            return managedDatabases.get( dbKey );
        }
    }

    @Override
    public void closeManaged( String dbKey )
    {
        checkNotNull( dbKey, "dbKey is null" );
        synchronized ( managedDatabases )
        {
            final DB db = managedDatabases.get( dbKey );
            if ( db != null )
            {
                closeSilently( dbKey, db );
            }
            managedDatabases.remove( dbKey );
        }
    }

    // == events

    @Subscribe
    public void on( final NexusStoppedEvent evt )
    {
        getLogger().info( "Closing all managed LevelDB instances..." );
        synchronized ( managedDatabases )
        {
            for ( Map.Entry<String, DB> entry : managedDatabases.entrySet() )
            {
                closeSilently( entry.getKey(), entry.getValue() );
            }
            managedDatabases.clear();
        }
    }

    // == internal

    protected DB create( final File file, final Options options )
        throws IOException
    {
        return Iq80DBFactory.factory.open( file, options );
    }

    protected void closeSilently( final String dbKey, final DB db )
    {
        try
        {
            getLogger().debug( "closing {}", dbKey );
            db.close();
        }
        catch ( Exception e )
        {
            getLogger().warn( "Problem while closing managed LevelDB {}", dbKey, e );
        }
    }
}
