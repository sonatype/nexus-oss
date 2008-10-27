package org.sonatype.nexus.integrationtests.nexus969;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus969CacheEvictInteractionTest
    extends AbstractNexusIntegrationTest
{

    private static final String CACHE_EVICT = "cache-evict";

    @Test
    public void testCacheAndEvict()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "Can't be kept active, is breaking all other tests" );
            return;
        }
        String id1 = createEvictTask( CACHE_EVICT ).getId();
        String id2 = createEvictTask( CACHE_EVICT + "2" ).getId();
        Assert.assertFalse( id1.equals( id2 ) );
        restart();
        String id3 = createEvictTask( CACHE_EVICT + "3" ).getId();
        Assert.assertFalse( "The new task ID should be different both are : " + id3, id1.equals( id3 ) );
        Assert.assertFalse( "The new task ID should be different both are: " + id3, id2.equals( id3 ) );
    }

    private void restart()
        throws Exception
    {
        // soft restart isn't enought to catch the bug
        // NexusStateUtil.doSoftRestart();
        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

        try
        {
            appBooter.stop();
            appBooter.start();
        }
        catch ( AppBooterServiceException e )
        {
            Assert.fail( "The Test failed to stop a forked JVM, so, it was either (most likely) not running or an orphaned process that you will need to kill." );
        }

    }

    private ScheduledServiceListResource createEvictTask( String taskName )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "all_repo" );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 0 ) );
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( taskName );
        scheduledTask.setTypeId( EvictUnusedItemsTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( age );
        scheduledTask.addProperty( repo );

        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertTrue( "Unable to create task: " + status.getDescription(), status.isSuccess() );

        return TaskScheduleUtil.getTask( taskName );
    }
}
