package org.sonatype.nexus.integrationtests.nexus533;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
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
    private void assertTasks()
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

        ScheduledServiceBaseResource taskManual = new ScheduledServiceBaseResource();
        taskManual.setId( task.getId() );
        taskManual.setName( scheduledTask.getName() );
        taskManual.setEnabled( true );
        taskManual.setTypeId( scheduledTask.getTypeId() );
        taskManual.setProperties( scheduledTask.getProperties() );

        Status status = TaskScheduleUtil.update( taskManual );
        Assert.assertTrue( status.isSuccess() );

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
