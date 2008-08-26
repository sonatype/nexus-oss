package org.sonatype.nexus.integrationtests.nexus636;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus533.TaskScheduleUtil;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

public class Nexus636EvictUnusedProxiedTaskTest
    extends AbstractNexusIntegrationTest
{

    private File repositoryPath;

    // private File artifactFolder;

    @Before
    public void deployOldArtifacts()
        throws Exception
    {
        repositoryPath = new File( nexusBaseDir, "runtime/work/storage/" + REPO_RELEASE_PROXY_REPO1 );

        File repo = getTestFile( "repo" );

        FileUtils.copyDirectory( repo, repositoryPath );
    }

    @Test
    public void clearProxy()
        throws Exception
    {
        executeTask( REPO_RELEASE_PROXY_REPO1, 0 );

        File[] files = repositoryPath.listFiles();
        Assert.assertEquals( "All files should be delete from repository", 0, files.length );
    }

    @Test
    public void keepTestDeployedFiles()
    throws Exception
    {
        executeTask( REPO_RELEASE_PROXY_REPO1, 1 );
        
        File artifact = new File( repositoryPath, "nexus636/artifact-new/1.0/artifact-new-1.0.jar" );
        Assert.assertTrue( "The files deployed by this test should be young enought to be kept", artifact.exists());
    }
    
    private void executeTask( String repository, int cacheAge )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( repository );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( cacheAge ) );

        // clean unused
        TaskScheduleUtil.runTask( "org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask", repo, age );

    }
}
