package org.sonatype.nexus.integrationtests.nexus836;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus836OOSRepoReindexTaskIT
    extends AbstractNexusIntegrationTest
{

    @Before
    public void putOutOfService()
        throws Exception
    {
        RepositoryStatusMessageUtil.putOutOfService( REPO_TEST_HARNESS_SHADOW, "hosted" );
    }

    @Test
    public void expireAllRepos()
        throws Exception
    {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "all_repo" );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ReindexTaskDescriptor.ID, prop );

        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }
}
