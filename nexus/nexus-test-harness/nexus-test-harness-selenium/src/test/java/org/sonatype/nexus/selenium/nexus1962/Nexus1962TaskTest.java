package org.sonatype.nexus.selenium.nexus1962;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.hamcrest.collection.IsCollectionContaining;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;
import org.sonatype.nexus.mock.pages.SchedulesConfigTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus1962TaskTest.class )
public class Nexus1962TaskTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

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
        doLogin();

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        // CREATE
        MockListener ml = MockHelper.listen( "/schedules", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                System.out.println( payload );
                assertNotNull( payload );
            }
        } );

        SchedulesConfigFormTab taskForm = scheduleGrid.newTask();
        taskForm.populate( true, "seleniumTask", "EmptyTrashTask", "Manual" ).save();

        ScheduledServiceResourceStatusResponse task = (ScheduledServiceResourceStatusResponse) ml.getResult();
        assertNotNull( task );
        String taskId = task.getData().getResource().getId();
        String uiTaskId = nexusBaseURL + "service/local/schedules/" + taskId;

        scheduleGrid.refresh();
        assertTrue( scheduleGrid.contains( uiTaskId ) );

        // READ
        scheduleGrid.refresh().select( uiTaskId );
        assertEquals( "seleniumTask", taskForm.getName().getValue() );
        assertEquals( "EmptyTrashTask", taskForm.getTaskType().getValue() );
        assertEquals( "Manual", taskForm.getRecurrence().getValue() );

        // UPDATE
        taskForm.getName().type( "seleniumTaskUpdated" );
        taskForm.save();
        scheduleGrid.refresh().select( 0 );
        assertEquals( "seleniumTaskUpdated", taskForm.getName().getValue() );

        // DELETE
        scheduleGrid.deleteTask().clickYes();

        Assert.assertEquals( 0, scheduleGrid.getStoreDataLength() );
    }

    @Requirement
    private NexusScheduler nexusScheduler;

    @Test
    public void contextMenuRefresh()
        throws InterruptedException
    {
        doLogin();

        SchedulesConfigTab tasks = main.openTasks();
        ScheduleGrid scheduleGrid = tasks.getScheduleGrid();

        ScheduledTask<Object> task =
            nexusScheduler.schedule( "selenium-context-task",
                                     nexusScheduler.createTaskInstance( EmptyTrashTask.class ), new ManualRunSchedule() );
        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

        // refresh
        tasks.contextMenuRefresh( 0 );
        assertThat( Arrays.asList( scheduleGrid.getKeys() ), IsCollectionContaining.hasItems( uiTaskId ) );
    }

    @Test
    public void contextMenuRun()
        throws InterruptedException
    {
        ScheduledTask<Object> task =
            nexusScheduler.schedule( "selenium-context-task",
                                     nexusScheduler.createTaskInstance( EmptyTrashTask.class ), new ManualRunSchedule() );

        doLogin();
        SchedulesConfigTab tasks = main.openTasks();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

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
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();
    }

    @Test
    public void contextMenuDelete()
        throws InterruptedException
    {
        doLogin();

        ScheduledTask<Object> task =
            nexusScheduler.schedule( "selenium-context-task",
                                     nexusScheduler.createTaskInstance( EmptyTrashTask.class ), new ManualRunSchedule() );
        SchedulesConfigTab tasks = main.openTasks();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

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
