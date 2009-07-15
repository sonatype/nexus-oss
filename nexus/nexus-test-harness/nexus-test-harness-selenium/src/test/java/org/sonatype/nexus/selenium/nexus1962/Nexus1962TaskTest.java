package org.sonatype.nexus.selenium.nexus1962;

import java.util.Arrays;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;
import org.sonatype.nexus.mock.pages.SchedulesConfigTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.ManualRunSchedule;

public class Nexus1962TaskTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        SchedulesConfigFormTab newTask = scheduleGrid.newTask();

        NxAssert.requiredField( newTask.getName(), "taskname" );
        NxAssert.requiredField( newTask.getTaskType(), 0 );
        NxAssert.requiredField( newTask.getRecurrence(), 0 );

        newTask.cancel();
    }

    @Test
    public void crudTask()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        // CREATE
        MockListener ml = MockHelper.listen( "/schedules", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                System.out.println( payload );
                Assert.assertNotNull( payload );
            }
        } );

        SchedulesConfigFormTab taskForm = scheduleGrid.newTask();
        taskForm.populate( true, "seleniumTask", "EmptyTrashTask", "Manual" ).save();

        ScheduledServiceResourceStatusResponse task = (ScheduledServiceResourceStatusResponse) ml.getResult();
        String taskId = task.getData().getResource().getId();
        String uiTaskId = nexusBaseURL + "service/local/schedules/" + taskId;

        scheduleGrid.refresh();
        Assert.assertTrue( scheduleGrid.contains( uiTaskId ) );

        // READ
        scheduleGrid.refresh().select( uiTaskId );
        Assert.assertEquals( "seleniumTask", taskForm.getName().getValue() );
        Assert.assertEquals( "EmptyTrashTask", taskForm.getTaskType().getValue() );
        Assert.assertEquals( "Manual", taskForm.getRecurrence().getValue() );

        // UPDATE
        taskForm.getName().type( "seleniumTaskUpdated" );
        taskForm.save();
        scheduleGrid.refresh().select( 0 );
        Assert.assertEquals( "seleniumTaskUpdated", taskForm.getName().getValue() );

        // DELETE
        scheduleGrid.deleteTask().clickYes();

        Assert.assertEquals( 0, scheduleGrid.getStoreDataLength() );
    }

    private NexusScheduler nexusScheduler;

    @Before
    public void createScheduler()
        throws ComponentLookupException
    {
        nexusScheduler = lookup( NexusScheduler.class );
    }

    @Test
    public void contextMenu()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        String taskName = "selenium-context-task";
        ScheduledTask<Object> task =
            nexusScheduler.schedule( taskName, nexusScheduler.createTaskInstance( EmptyTrashTask.class ),
                                     new ManualRunSchedule() );

        SchedulesConfigTab tasks = main.openTasks();
        ScheduleGrid scheduleGrid = tasks.getScheduleGrid();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

        // refresh
        tasks.contextMenuRefresh( 0 );
        Assert.assertThat( Arrays.asList( scheduleGrid.getKeys() ), IsCollectionContaining.hasItems( uiTaskId ) );

        // run
        MockHelper.listen( "/schedule_run/{scheduledServiceId}", new MockListener() );
        tasks.contextMenuRun( uiTaskId );

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        // run error
        MockHelper.expect( "/schedule_run/{scheduledServiceId}", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST,
                                                                                   null ) );
        tasks.contextMenuRun( uiTaskId );

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        // delete
        tasks.contextMenuDelete( uiTaskId );

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        try
        {
            nexusScheduler.getTaskById( task.getId() );
            Assert.fail( "Task shouldn't exists! " + task.getId() );
        }
        catch ( NoSuchTaskException e )
        {
            // expected
        }

    }

}
