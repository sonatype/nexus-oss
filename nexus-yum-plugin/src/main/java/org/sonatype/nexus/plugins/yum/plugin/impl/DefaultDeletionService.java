/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.yum.plugin.impl;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.yum.config.YumPluginConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.DeletionService;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.YumRegistry;

@Named
@Singleton
public class DefaultDeletionService
    implements DeletionService
{

    private final static Logger log = LoggerFactory.getLogger( DefaultDeletionService.class );

    private static final int POOL_SIZE = 10;

    private static final int MAX_EXECUTION_COUNT = 100;

    private final YumPluginConfiguration configuration;

    private final YumRegistry yumRegistry;

    @Inject
    public DefaultDeletionService(final YumPluginConfiguration configuration, final  YumRegistry yumRegistry)
    {
        this.configuration = configuration;
        this.yumRegistry = yumRegistry;
    }

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor( POOL_SIZE );

    private final Map<ScheduledFuture<?>, DelayedDirectoryDeletionTask> taskMap =
        new HashMap<ScheduledFuture<?>, DelayedDirectoryDeletionTask>();

    private final Map<DelayedDirectoryDeletionTask, ScheduledFuture<?>> reverseTaskMap =
        new HashMap<DelayedDirectoryDeletionTask, ScheduledFuture<?>>();

    @Override
    public void deleteRpm( Repository repository, String path )
    {
        if ( configuration.isDeleteProcessing() )
        {
            final DelayedDirectoryDeletionTask task = findDelayedParentDirectory( repository, path );
            if ( task != null )
            {
                activate( task );
            }
            else
            {
                final Yum yum = yumRegistry.get( repository.getId() );
                if ( yum != null )
                {
                    log.info( "Delete rpm {} / {}", repository.getId(), path );
                    yum.recreateRepository();
                }
            }
        }
    }

    private void activate( DelayedDirectoryDeletionTask task )
    {
        task.setActive( true );
    }

    private void schedule( DelayedDirectoryDeletionTask task )
    {
        final ScheduledFuture<?> future = executor.schedule( task, configuration.getDelayAfterDeletion(), SECONDS );
        taskMap.put( future, task );
        reverseTaskMap.put( task, future );
    }

    private DelayedDirectoryDeletionTask findDelayedParentDirectory( Repository repository, String path )
    {
        for ( Runnable runnable : executor.getQueue() )
        {
            DelayedDirectoryDeletionTask dirTask = taskMap.get( runnable );
            if ( dirTask != null && dirTask.isParent( repository, path ) )
            {
                return dirTask;
            }
        }
        return null;
    }

    @Override
    public void deleteDirectory( Repository repository, String path )
    {
        if ( configuration.isDeleteProcessing() )
        {
            if ( findDelayedParentDirectory( repository, path ) == null )
            {
                schedule( new DelayedDirectoryDeletionTask( this, repository, path ) );
            }
        }
    }

    @Override
    public void execute( DelayedDirectoryDeletionTask task )
    {
        final ScheduledFuture<?> future = reverseTaskMap.remove( task );
        if ( future != null )
        {
            taskMap.remove( future );
        }
        if ( task.isActive() )
        {
            if ( isDeleted( task.getRepository(), task.getPath() ) )
            {
                final Yum yum = yumRegistry.get( task.getRepository().getId() );
                if ( yum != null )
                {
                    log.info(
                        "Recreate yum repository {} because of removed path {}",
                        task.getRepository().getId(), task.getPath()
                    );
                    yum.recreateRepository();
                }
            }
            else if ( task.getExecutionCount() < MAX_EXECUTION_COUNT )
            {
                log.info( "Rescheduling creation of yum repository {} because path {} not deleted.",
                          task.getRepository().getId(), task.getPath() );
                schedule( task );
            }
            else
            {
                log.warn( "Deleting path {} in repository {} took too long - retried {} times.",
                          new Object[]{ task.getPath(), task.getRepository().getId(), MAX_EXECUTION_COUNT } );
            }
        }
    }

    private boolean isDeleted( Repository repository, String path )
    {
        try
        {
            repository.retrieveItem( new ResourceStoreRequest( path ) );
            return false;
        }
        catch ( Exception e )
        {
            return true;
        }
    }

}
