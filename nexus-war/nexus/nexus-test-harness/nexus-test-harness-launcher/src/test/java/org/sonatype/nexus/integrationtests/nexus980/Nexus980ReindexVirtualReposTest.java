package org.sonatype.nexus.integrationtests.nexus980;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Reindex a virtual repo should fail
 */
public class Nexus980ReindexVirtualReposTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void manualReindex()
        throws IOException
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( Nexus980ReindexVirtualReposTest.class, "manualReindex" );
            return;
        }
        String serviceURI = "service/local/data_index/repositories/nexus-test-harness-shadow/content";
        Status status = RequestFacade.sendMessage( serviceURI, Method.DELETE ).getStatus();
        Assert.assertFalse( "Should not being able to reindex a shadow repo", status.isSuccess() );
    }

    @Test
    public void taskReindex()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( Nexus980ReindexVirtualReposTest.class, "taskReindex" );
            return;
        }
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( REPO_TEST_HARNESS_SHADOW );

        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( "reindex_shadow" );
        scheduledTask.setTypeId( ReindexTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( prop );
        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertFalse( "Should not be able to create a reindex task to a virtual repo", status.isSuccess() );
    }

}
