package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.ReindexTask;

public class Nexus533TaskOnceTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceOnceResource>
{

    private static ScheduledServiceOnceResource scheduledTask;

    @Override
    public ScheduledServiceOnceResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceOnceResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskOnce" );
            scheduledTask.setSchedule( "once" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setStartDate( String.valueOf( startDate.getTime() ) );
            scheduledTask.setStartTime( "03:30" );

            scheduledTask.setTypeId( ReindexTask.HINT );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceOnceResource scheduledTask )
    {
        scheduledTask.setStartTime( "00:00" );
    }

}
