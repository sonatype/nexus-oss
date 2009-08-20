package org.sonatype.nexus.integrationtests.nexus1923;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1923GroupIncrementalIndex
    extends AbstractNexus1923
{
    public Nexus1923GroupIncrementalIndex()
        throws Exception
    {
        super();
    }

    @Test
    public void validateIncrementalIndexesCreated()
        throws Exception
    {
        createHostedRepository();
        createSecondHostedRepository();
        createThirdHostedRepository();
        
        TaskScheduleUtil.waitForAllTasksToStop();

        createGroup( GROUP_ID, HOSTED_REPO_ID, SECOND_HOSTED_REPO_ID, THIRD_HOSTED_REPO_ID );
        
        TaskScheduleUtil.waitForAllTasksToStop();

        String reindexId = createReindexTask( GROUP_ID, GROUP_REINDEX_TASK_NAME );

        // deploy artifact 1 on repo 1
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), getHostedRepositoryStorageDirectory() );

        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // repo 1 has index when it is created, so should create .1
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 0 );

        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 0 );

        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertFalse( getGroupIndexIncrement( "1" ).exists() );
        validateCurrentGroupIncrementalCounter( 0 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );

        // deploy artifact 2 on repo 2
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), getSecondHostedRepositoryStorageDirectory() );

        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // shouldn't changed from first status
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        // repo 2 has index when it is created, so should create .1
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );
        
        // shouldn't change from first status
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 0 );

        // group create index .1
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "2" ).exists() );
        validateCurrentGroupIncrementalCounter( 1 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );

        // deploy artifact 3 on repo 3
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ), getThirdHostedRepositoryStorageDirectory() );
        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // shouldn't changed from previous status
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        // shouldn't changed from previous status
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );

        // repo 3 has index when it is created, so should create .1
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertTrue( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 1 );

        // group create index .2
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "3" ).exists() );
        validateCurrentGroupIncrementalCounter( 2 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );
        searchFor( THIRD_HOSTED_REPO_ID, THIRD_ARTIFACT );

        // deploy artifact 4 on repo 1
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ), getHostedRepositoryStorageDirectory() );
        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // now repo 1 gets index .2
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "3" ).exists() );
        validateCurrentHostedIncrementalCounter( 2 );

        // shouldn't changed from previous status
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );

        // repo 3 has index when it is created, so should create .1
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertTrue( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 1 );

        // group create index .3
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "3" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "4" ).exists() );
        validateCurrentGroupIncrementalCounter( 3 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT, FOURTH_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );
        searchFor( THIRD_HOSTED_REPO_ID, THIRD_ARTIFACT );
    }

    private void searchFor( String whereRepo, String... whatForArtifacts )
        throws Exception
    {
        for ( String artifact : whatForArtifacts )
        {
            searchForArtifactInIndex( artifact, whereRepo, true );
            searchForArtifactInIndex( artifact, GROUP_ID, true );
        }
        
        List<String> otherArtifacts = getArtifactBut( whatForArtifacts );
        
        for ( String artifact : otherArtifacts )
        {
            searchForArtifactInIndex( artifact, whereRepo, false );
        }

        List<String> repos = getReposBut( whereRepo );
        for ( String repoId : repos )
        {
            for ( String artifact : whatForArtifacts )
            {
                searchForArtifactInIndex( artifact, repoId, false );
            }
        }
    }

    private List<String> getArtifactBut( String[] butArtifacts )
    {
        List<String> artifacts = new ArrayList<String>();
        artifacts.add( FIRST_ARTIFACT );
        artifacts.add( SECOND_ARTIFACT );
        artifacts.add( THIRD_ARTIFACT );
        artifacts.add( FOURTH_ARTIFACT );
        artifacts.add( FIFTH_ARTIFACT );
        artifacts.removeAll( Arrays.asList( butArtifacts ) );
        return artifacts;
    }

    private List<String> getReposBut( String butRepo )
    {
        List<String> repos = new ArrayList<String>();
        repos.add( HOSTED_REPO_ID );
        repos.add( SECOND_HOSTED_REPO_ID );
        repos.add( THIRD_HOSTED_REPO_ID );
        repos.remove( butRepo );
        return repos;
    }
}
