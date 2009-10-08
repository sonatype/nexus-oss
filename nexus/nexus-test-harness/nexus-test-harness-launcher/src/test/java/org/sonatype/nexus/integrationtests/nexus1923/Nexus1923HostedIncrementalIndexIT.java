package org.sonatype.nexus.integrationtests.nexus1923;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1923HostedIncrementalIndexIT
    extends AbstractNexus1923
{    
    public Nexus1923HostedIncrementalIndexIT()
        throws Exception
    {
        super();
    }
    
    @Test
    public void validateIncrementalIndexesCreated()
        throws Exception
    {
        File repoStorageDirectory = getHostedRepositoryStorageDirectory();
        
        //First create our repository
        createHostedRepository();
        
        TaskScheduleUtil.waitForAllTasksToStop();
        
        //Create the reindex task
        String reindexId = createHostedReindexTask();
        
        //Put an artifact in the storage
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), 
            repoStorageDirectory );
        
        //Now reindex the repo
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and no incremental files
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, false );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, false );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, false );
        
        //Put an artifact in the storage
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), 
            repoStorageDirectory );
        
        //Now reindex the repo
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 1 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "3" ).exists() );
        validateCurrentHostedIncrementalCounter( 2 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, false );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, false );
        
        //Put an artifact in the storage
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ), 
            repoStorageDirectory );
        
        //Now reindex the repo
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 2 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "4" ).exists() );
        validateCurrentHostedIncrementalCounter( 3 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, true );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, false );
        
        //Put an artifact in the storage
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ), 
            repoStorageDirectory );
        
        //Now reindex the repo
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 3 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "4" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "5" ).exists() );
        validateCurrentHostedIncrementalCounter( 4 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, true );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, false );
        
        //Put an artifact in the storage
        FileUtils.copyDirectoryStructure( getTestFile( FIFTH_ARTIFACT ), 
            repoStorageDirectory );
        
        //Now reindex the repo
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 4 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "4" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "5" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "6" ).exists() );
        validateCurrentHostedIncrementalCounter( 5 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, true );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, true );
        
        //Now reindex the repo again, and make sure nothing new is created
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 4 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "4" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "5" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "6" ).exists() );
        validateCurrentHostedIncrementalCounter( 5 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, true );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, true );
        
        //Now delete some items and put some back
        deleteAllNonHiddenContent( getHostedRepositoryStorageDirectory() );
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), 
            repoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), 
            repoStorageDirectory );
        
        //Reindex
        reindexHostedRepository( reindexId );
        
        //Now make sure there is an index file, and 5 incremental file
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "4" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "5" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "6" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "7" ).exists() );
        validateCurrentHostedIncrementalCounter( 6 );
        
        //Now make sure that the search is properly working
        searchForArtifactInHostedIndex( FIRST_ARTIFACT, true );
        searchForArtifactInHostedIndex( SECOND_ARTIFACT, true );
        searchForArtifactInHostedIndex( THIRD_ARTIFACT, false );
        searchForArtifactInHostedIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInHostedIndex( FIFTH_ARTIFACT, false );
    }
}
