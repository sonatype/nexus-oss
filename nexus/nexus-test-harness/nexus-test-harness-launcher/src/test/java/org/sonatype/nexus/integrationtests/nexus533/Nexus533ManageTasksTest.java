package org.sonatype.nexus.integrationtests.nexus533;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

public class Nexus533ManageTasksTest
    extends AbstractNexusIntegrationTest
{

    private ScheduledServiceOnceResource taskOnce;

    @Before
    public void createTasks()
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

    }
}
