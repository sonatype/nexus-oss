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
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.ID;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.yum.config.YumPluginConfiguration;
import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.repository.service.YumRepositoryCache;
import org.sonatype.nexus.plugins.yum.repository.task.TaskDoubledException;
import org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.rest.RepositoryURLBuilder;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import com.google.inject.assistedinject.Assisted;

@Named
public class YumImpl
    implements Yum
{

    private final RepositoryURLBuilder repositoryURLBuilder;

    private final NexusScheduler nexusScheduler;

    private final YumPluginConfiguration yumConfig;

    private final Repository repository;

    private final File baseDir;

    private final Set<String> versions = new HashSet<String>();

    @Inject
    public YumImpl( final RepositoryURLBuilder repositoryURLBuilder,
                    final NexusScheduler nexusScheduler,
                    final YumPluginConfiguration yumConfig,
                    final @Assisted Repository repository )
        throws MalformedURLException, URISyntaxException
    {
        this.repositoryURLBuilder = repositoryURLBuilder;

        this.nexusScheduler = nexusScheduler;
        this.yumConfig = yumConfig;
        this.repository = repository;

        this.baseDir = RepositoryUtils.getBaseDir( repository );
    }

    private final YumRepositoryCache cache = new YumRepositoryCache();

    public String getId()
    {
        return repository.getId();
    }

    public Set<String> getVersions()
    {
        return versions;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public void addVersion( String version )
    {
        versions.add( version );
    }

    @Override
    public Repository getRepository()
    {
        return repository;
    }

    @Override
    public ScheduledTask<YumRepository> createYumRepository( final String version,
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

    private ScheduledTask<YumRepository> submitTask( YumMetadataGenerationTask task )
    {
        try
        {
            return nexusScheduler.submit( ID, task );
        }
        catch ( TaskDoubledException e )
        {
            return mergeAddedFiles( e.getOriginal(), task );
        }
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

    @Override
    public ScheduledTask<YumRepository> createYumRepository()
    {
        return addToYumRepository( null );
    }

    @Override
    public YumRepository getYumRepository( final String version, final URL baseRepoUrl )
        throws Exception
    {
        YumRepository yumRepository = cache.lookup( repository.getId(), version );
        if ( ( yumRepository == null ) || yumRepository.isDirty() )
        {
            final ScheduledTask<YumRepository> future = createYumRepository(
                version, createRepositoryTempDir( repository, version ), baseRepoUrl
            );
            yumRepository = future.get();
            cache.cache( yumRepository );
        }
        return yumRepository;
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
    public ScheduledTask<YumRepository> addToYumRepository( String filePath )
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

    private YumMetadataGenerationTask createTask()
    {
        return nexusScheduler.createTaskInstance( YumMetadataGenerationTask.class );
    }

    private File createRepositoryTempDir( Repository repository, String version )
    {
        return new File( yumConfig.getBaseTempDir(), repository.getId() + File.separator + version );
    }
}
