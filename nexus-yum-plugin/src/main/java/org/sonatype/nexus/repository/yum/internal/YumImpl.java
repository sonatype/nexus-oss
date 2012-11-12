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
package org.sonatype.nexus.repository.yum.internal;

import static java.io.File.pathSeparator;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.nexus.repository.yum.internal.task.YumMetadataGenerationTask.ID;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.YumRepository;
import org.sonatype.nexus.repository.yum.internal.config.YumPluginConfiguration;
import org.sonatype.nexus.repository.yum.internal.task.TaskAlreadyScheduledException;
import org.sonatype.nexus.repository.yum.internal.task.YumMetadataGenerationTask;
import org.sonatype.nexus.rest.RepositoryURLBuilder;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import com.google.inject.assistedinject.Assisted;

@Named
public class YumImpl
    implements Yum
{

    private final static Logger log = LoggerFactory.getLogger( YumImpl.class );

    private final RepositoryURLBuilder repositoryURLBuilder;

    private final NexusScheduler nexusScheduler;

    private final YumPluginConfiguration yumConfig;

    private final ScheduledThreadPoolExecutor executor;

    private final Repository repository;

    private final File baseDir;

    private final Set<String> versions = new HashSet<String>();

    private static final int MAX_EXECUTION_COUNT = 100;

    private final Map<ScheduledFuture<?>, DelayedDirectoryDeletionTask> taskMap =
        new HashMap<ScheduledFuture<?>, DelayedDirectoryDeletionTask>();

    private final Map<DelayedDirectoryDeletionTask, ScheduledFuture<?>> reverseTaskMap =
        new HashMap<DelayedDirectoryDeletionTask, ScheduledFuture<?>>();

    @Inject
    public YumImpl( final RepositoryURLBuilder repositoryURLBuilder,
                    final NexusScheduler nexusScheduler,
                    final YumPluginConfiguration yumConfig,
                    final ScheduledThreadPoolExecutor executor,
                    final @Assisted Repository repository )
        throws MalformedURLException, URISyntaxException
    {
        this.repositoryURLBuilder = repositoryURLBuilder;
        this.nexusScheduler = nexusScheduler;
        this.yumConfig = yumConfig;
        this.executor = executor;
        this.repository = repository;

        this.baseDir = RepositoryUtils.getBaseDir( repository );
    }

    private final YumRepositoryCache cache = new YumRepositoryCache();

    @Override
    public Set<String> getVersions()
    {
        return versions;
    }

    @Override
    public File getBaseDir()
    {
        return baseDir;
    }

    @Override
    public void addVersion( final String version )
    {
        versions.add( version );
        log.debug( "Added version '{}' to repository '{}", version, getRepository().getId() );
    }

    @Override
    public Repository getRepository()
    {
        return repository;
    }

    ScheduledTask<YumRepository> createYumRepository( final String version,
                                                      final File yumRepoBaseDir,
                                                      final URL yumRepoUrl )
    {
        try
        {
            File rpmBaseDir = RepositoryUtils.getBaseDir( repository );
            if ( yumConfig.isActive() )
            {
                YumMetadataGenerationTask task = createTask();
                task.setRpmDir( rpmBaseDir.getAbsolutePath() );
                task.setRpmUrl( repositoryURLBuilder.getRepositoryContentUrl( repository ) );
                task.setRepoDir( yumRepoBaseDir );
                task.setRepoUrl( yumRepoUrl.toString() );
                task.setRepositoryId( repository.getId() );
                task.setVersion( version );
                return submitTask( task );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Unable to create repository", e );
        }

        return null;
    }

    @Override
    public YumRepository getYumRepository( final String version, final URL baseRepoUrl )
        throws Exception
    {
        YumRepositoryImpl yumRepository = cache.lookup( repository.getId(), version );
        if ( ( yumRepository == null ) || yumRepository.isDirty() )
        {
            final ScheduledTask<YumRepository> future = createYumRepository(
                version, createRepositoryTempDir( repository, version ), baseRepoUrl
            );
            yumRepository = (YumRepositoryImpl) future.get();
            cache.cache( yumRepository );
        }
        return yumRepository;
    }

    private ScheduledTask<YumRepository> submitTask( YumMetadataGenerationTask task )
    {
        try
        {
            return nexusScheduler.submit( ID, task );
        }
        catch ( TaskAlreadyScheduledException e )
        {
            return mergeAddedFiles( e.getOriginal(), task );
        }
    }

    @Override
    public void recreateRepository()
    {
        createYumRepository();
    }

    @Override
    public void markDirty( final String itemVersion )
    {
        cache.markDirty( repository.getId(), itemVersion );
    }

    @Override
    public ScheduledTask<YumRepository> addToYumRepository( @Nullable String filePath )
    {
        try
        {
            File rpmBaseDir = RepositoryUtils.getBaseDir( repository );
            if ( yumConfig.isActive() )
            {
                YumMetadataGenerationTask task = createTask();
                task.setRpmDir( rpmBaseDir.getAbsolutePath() );
                task.setRpmUrl( repositoryURLBuilder.getRepositoryContentUrl( repository ) );
                task.setRepositoryId( repository.getId() );
                task.setAddedFiles( filePath );
                return submitTask( task );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Unable to create repository", e );
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    private ScheduledTask<YumRepository> mergeAddedFiles( ScheduledTask<?> existingScheduledTask,
                                                          YumMetadataGenerationTask taskToMerge )
    {
        if ( isNotBlank( taskToMerge.getAddedFiles() ) )
        {
            final YumMetadataGenerationTask existingTask = (YumMetadataGenerationTask) existingScheduledTask.getTask();
            if ( isBlank( existingTask.getAddedFiles() ) )
            {
                existingTask.setAddedFiles( taskToMerge.getAddedFiles() );
            }
            else
            {
                existingTask.setAddedFiles(
                    existingTask.getAddedFiles() + pathSeparator + taskToMerge.getAddedFiles() );
            }
        }
        return (ScheduledTask<YumRepository>) existingScheduledTask;
    }

    ScheduledTask<YumRepository> createYumRepository()
    {
        return addToYumRepository( null );
    }

    private YumMetadataGenerationTask createTask()
    {
        final YumMetadataGenerationTask task = nexusScheduler.createTaskInstance( YumMetadataGenerationTask.class );
        if ( task == null )
        {
            throw new IllegalStateException(
                "Could not create a task fo type " + YumMetadataGenerationTask.class.getName()
            );
        }
        return task;
    }

    private File createRepositoryTempDir( Repository repository, String version )
    {
        return new File( yumConfig.getBaseTempDir(), repository.getId() + File.separator + version );
    }

    @Override
    public void deleteRpm( String path )
    {
        if ( yumConfig.isDeleteProcessing() )
        {
            if ( findDelayedParentDirectory( path ) == null )
            {
                log.info( "Delete rpm {} / {}", repository.getId(), path );
                recreateRepository();
            }
        }
    }

    @Override
    public void deleteDirectory( String path )
    {
        if ( yumConfig.isDeleteProcessing() )
        {
            if ( findDelayedParentDirectory( path ) == null )
            {
                schedule( new DelayedDirectoryDeletionTask( path ) );
            }
        }
    }

    private void schedule( DelayedDirectoryDeletionTask task )
    {
        final ScheduledFuture<?> future = executor.schedule( task, yumConfig.getDelayAfterDeletion(), SECONDS );
        taskMap.put( future, task );
        reverseTaskMap.put( task, future );
    }

    private DelayedDirectoryDeletionTask findDelayedParentDirectory( final String path )
    {
        for ( final Runnable runnable : executor.getQueue() )
        {
            DelayedDirectoryDeletionTask dirTask = taskMap.get( runnable );
            if ( dirTask != null && path.startsWith( dirTask.path ) )
            {
                return dirTask;
            }
        }
        return null;
    }

    private boolean isDeleted( String path )
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

    private class DelayedDirectoryDeletionTask
        implements Runnable
    {

        private final String path;

        private int executionCount = 0;

        private DelayedDirectoryDeletionTask( final String path )
        {
            this.path = path;
        }

        @Override
        public void run()
        {
            executionCount++;
            final ScheduledFuture<?> future = reverseTaskMap.remove( this );
            if ( future != null )
            {
                taskMap.remove( future );
            }
            if ( isDeleted( path ) )
            {
                log.info(
                    "Recreate yum repository {} because of removed path {}", getRepository().getId(), path
                );
                recreateRepository();
            }
            else if ( executionCount < MAX_EXECUTION_COUNT )
            {
                log.info(
                    "Rescheduling creation of yum repository {} because path {} not deleted.",
                    getRepository().getId(), path
                );
                schedule( this );
            }
            else
            {
                log.warn(
                    "Deleting path {} in repository {} took too long - retried {} times.",
                    path, getRepository().getId(), MAX_EXECUTION_COUNT
                );
            }
        }
    }

}
