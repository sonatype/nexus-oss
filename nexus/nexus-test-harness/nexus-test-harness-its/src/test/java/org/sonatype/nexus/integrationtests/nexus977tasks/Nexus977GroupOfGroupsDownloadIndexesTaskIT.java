package org.sonatype.nexus.integrationtests.nexus977tasks;

import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.DownloadIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsDownloadIndexesTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void downloadIndexes()
        throws Exception
    {
        Assert.assertTrue( getSearchMessageUtil().searchForGav( getTestId(), "project", "0.8" ).isEmpty() );
        Assert.assertTrue( getSearchMessageUtil().searchForGav( getTestId(), "project", "2.1" ).isEmpty() );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( "DownloadIndexesTaskDescriptor-snapshot", DownloadIndexesTaskDescriptor.ID, repo );
        TaskScheduleUtil.waitForAllTasksToStop();
        Assert.assertNotNull( task, "The ScheduledServicePropertyResource task didn't run" );

        Assert.assertFalse( getSearchMessageUtil().searchForGav( getTestId(), "project", "0.8" ).isEmpty() );
        Assert.assertFalse( getSearchMessageUtil().searchForGav( getTestId(), "project", "2.1" ).isEmpty() );
    }

}
