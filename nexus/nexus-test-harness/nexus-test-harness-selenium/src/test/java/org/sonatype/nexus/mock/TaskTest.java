package org.sonatype.nexus.mock;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;

public class TaskTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        SchedulesConfigFormTab newTask = scheduleGrid.newTask().save();

        Assert.assertTrue( "Name is a required field", newTask.getName().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Task type is a required field",
                           newTask.getTaskType().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Recurrence is a required field",
                           newTask.getRecurrence().hasErrorText( "This field is required" ) );
    }

    @Test
    public void crudTask()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        //CREATE
        SchedulesConfigFormTab taskForm = scheduleGrid.newTask();
        taskForm.populate( true, "seleniumTask", "EmptyTrashTask", "Manual" ).save();

        //READ
        scheduleGrid.getRefresh().select( 0 );
        Assert.assertEquals( "seleniumTask", taskForm.getName().getValue() );
        Assert.assertEquals( "EmptyTrashTask", taskForm.getTaskType().getValue() );
        Assert.assertEquals( "Manual", taskForm.getRecurrence().getValue() );

        //UPDATE
        taskForm.getName().type( "seleniumTaskUpdated" );
        taskForm.save();
        scheduleGrid.getRefresh().select( 0 );
        Assert.assertEquals( "seleniumTaskUpdated", taskForm.getName().getValue() );

        //DELETE
        scheduleGrid.deleteTask().clickYes();

        Assert.assertEquals( 0, scheduleGrid.getStoreDataLength() );
    }

}
