package org.sonatype.nexus.integrationtests.nexus533;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractNexusTasksIntegrationTest<E extends ScheduledServiceBaseResource>
    extends AbstractNexusIntegrationTest
{

    public abstract E getTaskScheduled();

    @Test
    public void scheduleTasks()
        throws Exception
    {
        Status status = TaskScheduleUtil.create( getTaskScheduled() );
        Assert.assertTrue( status.isSuccess() );

        assertTasks();
    }

    @SuppressWarnings( "unchecked" )
    protected void assertTasks()
        throws IOException
    {
        Configuration nexusConfig = NexusConfigUtil.getNexusConfig();

        List<CScheduledTask> tasks = nexusConfig.getTasks();
        Assert.assertEquals( 1, tasks.size() );

        CScheduledTask task = tasks.get( 0 );
        E scheduledTask = getTaskScheduled();

        Assert.assertEquals( scheduledTask.getName(), task.getName() );
        Assert.assertEquals( scheduledTask.getTypeId(), task.getType() );
    }

    @Test
    public void updateTasks()
        throws Exception
    {
        E scheduledTask = getTaskScheduled();
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( scheduledTask.getName() );

        scheduledTask.setId( task.getId() );
        updateTask( scheduledTask );
        Status status = TaskScheduleUtil.update( scheduledTask );
        Assert.assertTrue( status.isSuccess() );

        assertTasks();
    }

    public abstract void updateTask( E scheduledTask );

    @Test
    public void changeScheduling()
        throws Exception
    {
        E scheduledTask = getTaskScheduled();
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( scheduledTask.getName() );

        // if we have a manual task we can't change the schedule to be manual again
        if ( !task.getSchedule().equals( "manual" ) )
        {

            ScheduledServiceBaseResource taskManual = new ScheduledServiceBaseResource();
            taskManual.setId( task.getId() );
            taskManual.setName( scheduledTask.getName() );
            taskManual.setEnabled( true );
            taskManual.setTypeId( scheduledTask.getTypeId() );
            taskManual.setProperties( scheduledTask.getProperties() );
            taskManual.setSchedule( "manual" );

            Status status = TaskScheduleUtil.update( taskManual );
            Assert.assertTrue( status.isSuccess() );

        }
        else
        {
            ScheduledServiceOnceResource updatedTask = new ScheduledServiceOnceResource();
            updatedTask.setId( task.getId() );
            updatedTask.setName( scheduledTask.getName() );
            updatedTask.setEnabled( task.isEnabled() );
            updatedTask.setTypeId( scheduledTask.getTypeId() );
            updatedTask.setProperties( scheduledTask.getProperties() );
            updatedTask.setSchedule( "once" );
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            updatedTask.setStartDate( String.valueOf( startDate.getTime() ) );
            updatedTask.setStartTime( "03:30" );

            Status status = TaskScheduleUtil.update( updatedTask );
            Assert.assertTrue( status.isSuccess() );
        }

        assertTasks();
    }

    @Test
    public void deleteTasks()
        throws Exception
    {
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( getTaskScheduled().getName() );
        Status status = TaskScheduleUtil.deleteTask( task.getId() );
        Assert.assertTrue( status.isSuccess() );

        // delete is not working, see NEXUS-572
        // Configuration nexusConfig = NexusConfigUtil.getNexusConfig();
        // Assert.assertTrue( nexusConfig.getTasks().isEmpty() );
    }

}
