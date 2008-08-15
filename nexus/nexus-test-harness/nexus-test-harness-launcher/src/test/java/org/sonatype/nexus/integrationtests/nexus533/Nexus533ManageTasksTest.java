package org.sonatype.nexus.integrationtests.nexus533;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.NexusConfigUtil;

public class Nexus533ManageTasksTest
    extends AbstractNexusIntegrationTest
{

    private static ScheduledServiceOnceResource taskOnce;

    @BeforeClass
    public static void createTasks()
    {
        taskOnce = new ScheduledServiceOnceResource();
        taskOnce.setEnabled( true );
        taskOnce.setId( null );
        taskOnce.setName( "taskOnce" );
        // A future date
        Date startDate = DateUtils.addDays( new Date(), 10 );
        startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
        taskOnce.setStartDate( String.valueOf( startDate.getTime() ) );
        taskOnce.setStartTime( "03:30" );

        taskOnce.setTypeId( "org.sonatype.nexus.tasks.ReindexTask" );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "all_repo" );
        taskOnce.addProperty( prop );
    }

    @Test
    public void scheduleTasks()
        throws Exception
    {
        Status status = TaskScheduleUtil.create( taskOnce );
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

        for ( CScheduledTask task : tasks )
        {
            Assert.assertEquals( taskOnce.getName(), task.getName() );
            Assert.assertEquals( taskOnce.getTypeId(), task.getType() );
        }
    }

    @Test
    public void updateTasks()
        throws Exception
    {
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( taskOnce.getName() );
        taskOnce.setId( task.getId() );
        taskOnce.setStartTime( "00:00" );
        Status status = TaskScheduleUtil.update( taskOnce );
        Assert.assertTrue( status.isSuccess() );

        assertTasks();
    }

    @Test
    public void changeScheduling()
        throws Exception
    {
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( taskOnce.getName() );

        ScheduledServiceDailyResource taskDaily = new ScheduledServiceDailyResource();
        taskDaily.setId( task.getId() );
        taskDaily.setName( taskOnce.getName() );
        taskDaily.setEnabled( true );
        taskDaily.setRecurringTime( taskOnce.getStartTime() );
        taskDaily.setStartDate( taskOnce.getStartDate() );
        taskDaily.setTypeId( taskOnce.getTypeId() );
        taskDaily.setProperties( taskOnce.getProperties() );

        Status status = TaskScheduleUtil.update( taskDaily );
        Assert.assertTrue( status.isSuccess() );

        assertTasks();
    }

    @Test
    public void deleteTasks()
        throws Exception
    {
        ScheduledServiceListResource task = TaskScheduleUtil.getTask( taskOnce.getName() );
        Status status = TaskScheduleUtil.deleteTask( task.getId() );
        Assert.assertTrue( status.isSuccess() );

        Configuration nexusConfig = NexusConfigUtil.getNexusConfig();

        Assert.assertTrue( nexusConfig.getTasks().isEmpty() );
    }

}
