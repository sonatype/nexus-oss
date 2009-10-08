package org.sonatype.nexus.integrationtests.nexus1923;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1923ProxyIncrementalDeleteIT
extends AbstractNexus1923
{
    public Nexus1923ProxyIncrementalDeleteIT()
        throws Exception
    {
        super();
    }

    @Test
    public void validateIncrementalIndexesForDeleteCreated()
        throws Exception
    {
        File hostedRepoStorageDirectory = getHostedRepositoryStorageDirectory();

        //First create our hosted repository
        createHostedRepository();
        //And hosted repository task
        String hostedReindexId = createHostedReindexTask();
        //index hosted repo
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( FIFTH_ARTIFACT ),
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );

        //Now create our proxy repository
        createProxyRepository();

        //will download the initial index because repo has download remote set to true
        TaskScheduleUtil.waitForAllTasksToStop();

        //Now make sure that the search is properly working
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, true );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, true );

        //Now delete some items and put some back
        deleteAllNonHiddenContent( getHostedRepositoryStorageDirectory() );
        deleteAllNonHiddenContent( getProxyRepositoryStorageDirectory() );
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ),
            hostedRepoStorageDirectory );

        //Reindex
        reindexHostedRepository( hostedReindexId );

        String proxyReindexId = createProxyReindexTask();

        //reindex proxy and make sure we cant search for the now missing items
        reindexProxyRepository( proxyReindexId );

        //Make sure the indexes exist, and that a new one has been created with
        //the deletes
        Assert.assertTrue( getProxyRepositoryIndex().exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getProxyRepositoryIndexIncrement( "2" ).exists() );

        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, false );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
    }
}