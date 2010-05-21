package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus977GroupOfGroupsEvictUnusedProxiedItemsTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void evictUnused()
        throws Exception
    {
        downloadArtifactFromGroup( "g4", GavUtil.newGav( getTestId(), "project", "0.8" ),
                                   "target/downloads/nexus977evict" );
        downloadArtifactFromGroup( "g4", GavUtil.newGav( getTestId(), "project", "2.1" ),
                                   "target/downloads/nexus977evict" );

        Assert.assertTrue( new File( nexusWorkDir, "storage/r4/nexus977tasks/project/0.8/project-0.8.jar" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r5/nexus977tasks/project/2.1/project-2.1.jar" ).exists() );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 0 ) );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask( EvictUnusedItemsTaskDescriptor.ID, repo, age );
        TaskScheduleUtil.waitForAllTasksToStop();
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        Assert.assertFalse( new File( nexusWorkDir, "storage/r4/nexus977tasks/project/0.8/project-0.8.jar" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/r5/nexus977tasks/project/2.1/project-2.1.jar" ).exists() );

    }

}
