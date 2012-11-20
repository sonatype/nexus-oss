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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.yum.internal.task.YumGroupRepositoryGenerationTask.ID;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils;
import org.sonatype.nexus.test.os.IgnoreOn;
import org.sonatype.nexus.test.os.OsTestRule;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

public class YumGroupRepositoryGenerationTaskTest
    extends TestSupport
{

    private static final String GROUP_ID_1 = "group-repo-id-1";

    private static final String GROUP_ID_2 = "group-repo-id-2";

    private static final String MEMBER_ID_1 = "repo1";

    private static final String MEMBER_ID_2 = "repo2";

    private GroupRepository groupRepo;

    @Rule
    public OsTestRule osTestRule = new OsTestRule();

    @Test
    @IgnoreOn( "mac" )
    public void shouldGenerateGroupRepo()
        throws Exception
    {
        givenGroupRepoWith2YumRepos();
        thenGenerateYumRepo();
        RepositoryTestUtils.assertRepository( util.resolveFile( "target/tmp/group-repo/repodata" ), "group-repo" );
    }

    @Test
    public void shouldNotAllowConcurrentExecutionForSameRepo()
        throws Exception
    {
        final YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask( mock( EventBus.class ) );
        final GroupRepository groupRepo = mock( GroupRepository.class );
        when( groupRepo.getId() ).thenReturn( GROUP_ID_1 );
        task.setGroupRepository( groupRepo );
        assertThat( task.allowConcurrentExecution( createRunningTaskForRepos( groupRepo ) ), is( false ) );
    }

    @Test
    public void shouldNotAllowConcurrentExecutionIfAnotherTaskIsRunning()
        throws Exception
    {
        final YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask( mock( EventBus.class ) );
        final GroupRepository groupRepo = mock( GroupRepository.class );
        when( groupRepo.getId() ).thenReturn( GROUP_ID_1 );
        final GroupRepository groupRepo2 = mock( GroupRepository.class );
        when( groupRepo2.getId() ).thenReturn( GROUP_ID_2 );
        task.setGroupRepository( groupRepo );
        assertThat( task.allowConcurrentExecution( createRunningTaskForRepos( groupRepo2 ) ), is( false ) );
    }

    private Map<String, List<ScheduledTask<?>>> createRunningTaskForRepos( GroupRepository... groupRepos )
    {
        final Map<String, List<ScheduledTask<?>>> map = new HashMap<String, List<ScheduledTask<?>>>();
        final List<ScheduledTask<?>> taskList = new ArrayList<ScheduledTask<?>>();
        for ( GroupRepository groupRepo : groupRepos )
        {
            taskList.add( runningTask( groupRepo ) );
        }
        map.put( ID, taskList );
        return map;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private ScheduledTask<?> runningTask( GroupRepository groupRepo )
    {
        final ScheduledTask<?> task = mock( ScheduledTask.class );
        final YumGroupRepositoryGenerationTask otherGenerationTask = mock( YumGroupRepositoryGenerationTask.class );
        when( otherGenerationTask.getGroupRepository() ).thenReturn( groupRepo );
        when( task.getTaskState() ).thenReturn( RUNNING );
        when( task.getTask() ).thenReturn( (Callable) otherGenerationTask );
        return task;
    }

    private void thenGenerateYumRepo()
        throws Exception
    {
        YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask( mock( EventBus.class ) );
        task.setGroupRepository( groupRepo );
        task.doRun();
    }

    private void givenGroupRepoWith2YumRepos()
        throws IOException
    {
        final File groupRepoDir = util.resolveFile( "target/tmp/group-repo" );
        groupRepo = mock( GroupRepository.class );
        when( groupRepo.getLocalUrl() ).thenReturn( groupRepoDir.getAbsolutePath() );
        List<Repository> repositories = asList( createRepo( MEMBER_ID_1 ), createRepo( MEMBER_ID_2 ) );
        when( groupRepo.getMemberRepositories() ).thenReturn( repositories );
        if ( groupRepoDir.exists() )
        {
            FileUtils.deleteDirectory( groupRepoDir );
        }
        groupRepoDir.mkdirs();
    }

    private Repository createRepo( final String repositoryId )
    {
        final Repository repo = mock( Repository.class );
        final RepositoryKind kind = mock( RepositoryKind.class );
        when( kind.isFacetAvailable( Matchers.eq( MavenHostedRepository.class ) ) ).thenReturn( true );
        when( repo.getLocalUrl() ).thenReturn(
            util.resolveFile( "src/test/yum-repo/" + repositoryId ).getAbsolutePath()
        );
        when( repo.getRepositoryKind() ).thenReturn( kind );
        return repo;
    }
}
