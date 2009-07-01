package org.sonatype.nexus.selenium.nexus1962;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;

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

        scheduleGrid.refresh();
        scheduleGrid.contains( taskId );

        // READ
        scheduleGrid.refresh().select( 0 );
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

}
