package org.sonatype.nexus.integrationtests.nexus1719;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1719OOSRepoEvictUnusedTaskTest
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

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 10 ) );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask( EvictUnusedItemsTaskDescriptor.ID, prop, age );

        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }
}
