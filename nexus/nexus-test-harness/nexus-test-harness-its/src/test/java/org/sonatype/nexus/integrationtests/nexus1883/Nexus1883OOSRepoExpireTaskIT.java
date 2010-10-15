package org.sonatype.nexus.integrationtests.nexus1883;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1883OOSRepoExpireTaskIT
    extends AbstractNexusIntegrationTest
{

    @BeforeMethod
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
        prop.setKey( "repositoryOrGroupId" );
        prop.setValue( "repo_" + REPO_TEST_HARNESS_SHADOW );

        TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );
    }
}
