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
package org.sonatype.nexus.repository.yum.internal.task;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.YumRepository;
import org.sonatype.nexus.repository.yum.internal.RepositoryUtils;
import org.sonatype.nexus.repository.yum.internal.YumRepositoryImpl;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named( YumGroupRepositoryGenerationTask.ID )
public class YumGroupRepositoryGenerationTask
    extends AbstractNexusTask<YumRepository>
{

    private static final Logger LOG = LoggerFactory.getLogger( YumGroupRepositoryGenerationTask.class );

    public static final String ID = "YumGroupRepositoryGenerationTask";

    private static final int MAXIMAL_PARALLEL_RUNS = 1;

    private GroupRepository groupRepository;

    @Inject
    public YumGroupRepositoryGenerationTask( final EventBus eventBus )
    {
        super( eventBus, null );
    }

    public void setGroupRepository( final GroupRepository groupRepository )
    {
        this.groupRepository = groupRepository;
    }

    @Override
    protected YumRepository doRun()
        throws Exception
    {
        if ( isValidRepository() )
        {
            deleteYumTempDirs();

            final File repoBaseDir = RepositoryUtils.getBaseDir( groupRepository );
            final List<File> memberReposBaseDirs = getMemberReposBaseDirs();
            if ( memberReposBaseDirs.size() > 1 )
            {
                LOG.debug( "Merging repository group {}='{}' ...", groupRepository.getId(), groupRepository.getName() );
                new CommandLineExecutor().exec( buildCommand( repoBaseDir, memberReposBaseDirs ) );
                LOG.debug( "Group repository {}='{}' merged.", groupRepository.getId(), groupRepository.getName() );
            }
            else
            {
                final File groupRepoData = new File( repoBaseDir, "repodata" );
                LOG.debug(
                    "Remove group repository repodata, because at maximum one yum member-repository left : {}",
                    groupRepoData
                );
                // TODO this should be done via repo API
                deleteQuietly( groupRepoData );
            }

            deleteYumTempDirs();

            return new YumRepositoryImpl( repoBaseDir, groupRepository.getId(), null );
        }
        return null;
    }

    private List<File> getMemberReposBaseDirs()
        throws URISyntaxException, MalformedURLException
    {
        final List<File> memberRepoBaseDirs = new ArrayList<File>();
        for ( Repository memberRepository : groupRepository.getMemberRepositories() )
        {
            final File memberRepoBaseDir = RepositoryUtils.getBaseDir( memberRepository );
            if ( new File( memberRepoBaseDir, "repodata/repomd.xml" ).exists() )
            {
                memberRepoBaseDirs.add( memberRepoBaseDir );
            }
        }
        return memberRepoBaseDirs;
    }

    private void deleteYumTempDirs()
        throws IOException
    {
        final String yumTmpDirPrefix = "yum-" + System.getProperty( "user.name" );
        final File tmpDir = new File( "/var/tmp" );
        if ( tmpDir.exists() )
        {
            final File[] yumTmpDirs = tmpDir.listFiles( new FilenameFilter()
            {

                @Override
                public boolean accept( File dir, String name )
                {
                    return name.startsWith( yumTmpDirPrefix );
                }
            } );
            for ( File yumTmpDir : yumTmpDirs )
            {
                LOG.debug( "Deleting yum temp dir : {}", yumTmpDir );
                deleteQuietly( yumTmpDir );
            }
        }
    }

    @Override
    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {

        if ( activeTasks.containsKey( ID ) )
        {
            int activeRunningTasks = 0;
            for ( ScheduledTask<?> scheduledTask : activeTasks.get( ID ) )
            {
                if ( RUNNING.equals( scheduledTask.getTaskState() ) )
                {
                    if ( conflictsWith( (YumGroupRepositoryGenerationTask) scheduledTask.getTask() ) )
                    {
                        return false;
                    }
                    activeRunningTasks++;
                }
            }
            return activeRunningTasks < MAXIMAL_PARALLEL_RUNS;
        }
        else
        {
            return true;
        }
    }

    private boolean conflictsWith( YumGroupRepositoryGenerationTask task )
    {
        return task.getGroupRepository() != null && this.getGroupRepository() != null
            && task.getGroupRepository().getId().equals( getGroupRepository().getId() );
    }

    @Override
    protected String getAction()
    {
        return "GENERATE_YUM_GROUP_REPOSITORY";
    }

    @Override
    protected String getMessage()
    {
        return format( "Generate yum metadata for group repository %s='%s'", groupRepository.getId(),
                       groupRepository.getName() );
    }

    public GroupRepository getGroupRepository()
    {
        return groupRepository;
    }

    private boolean isValidRepository()
    {
        return groupRepository != null && !groupRepository.getMemberRepositories().isEmpty();
    }

    private String buildCommand( File repoBaseDir, List<File> memberRepoBaseDirs )
        throws MalformedURLException, URISyntaxException
    {
        final StringBuilder repos = new StringBuilder();
        for ( File memberRepoBaseDir : memberRepoBaseDirs )
        {
            repos.append( " --repo=" );
            repos.append( memberRepoBaseDir.toURI().toString() );
        }
        return format( "mergerepo --nogroups -d %s -o %s", repos.toString(), repoBaseDir.getAbsolutePath() );
    }

    public static ScheduledTask<YumRepository> createTaskFor( final NexusScheduler nexusScheduler,
                                                              final GroupRepository groupRepository )
    {
        final YumGroupRepositoryGenerationTask task = nexusScheduler.createTaskInstance(
            YumGroupRepositoryGenerationTask.class
        );
        task.setGroupRepository( groupRepository );
        return nexusScheduler.submit( YumGroupRepositoryGenerationTask.ID, task );
    }

}
