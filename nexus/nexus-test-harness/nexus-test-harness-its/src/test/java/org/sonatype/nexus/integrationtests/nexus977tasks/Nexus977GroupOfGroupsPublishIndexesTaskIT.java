package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.DownloadIndexesTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.PublishIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsPublishIndexesTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        // first must be sure there is an index to be published
        RepositoryMessageUtil.updateIndexes( "r1", "r2", "r3" );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repository_r4" );
        TaskScheduleUtil.runTask( "r4", DownloadIndexesTaskDescriptor.ID, repo );
        
        repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repository_r5" );
        TaskScheduleUtil.runTask( "r5", DownloadIndexesTaskDescriptor.ID, repo );
    }

    @Test
    public void publishIndexes()
        throws Exception
    {
        Assert.assertFalse( new File( nexusWorkDir, "storage/g1/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g2/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g3/.index" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/g4/.index" ).exists() );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        TaskScheduleUtil.runTask( "PublishIndexesTaskDescriptor-snapshot", PublishIndexesTaskDescriptor.ID, repo );
        
        Assert.assertTrue( new File( nexusWorkDir, "storage/r1/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r2/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r3/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r4/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r5/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g1/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g2/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g3/.index" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/g4/.index" ).exists() );
    }

}
