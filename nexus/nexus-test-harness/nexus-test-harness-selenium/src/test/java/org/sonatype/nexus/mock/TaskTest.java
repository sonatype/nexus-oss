package org.sonatype.nexus.mock;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;

public class TaskTest
    extends SeleniumTest
{

    @Test
    public void newTask()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        SchedulesConfigFormTab newTask = scheduleGrid.newTask();
        newTask.populate( true, "seleniumTask", "EmptyTrashTask", "Manual" ).save();
        scheduleGrid.getRefresh().select( 0 );

        Assert.assertEquals( "seleniumTask", newTask.getName().getValue() );
    }

}
