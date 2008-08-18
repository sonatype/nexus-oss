package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Nexus533TaskCronTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceMonthlyResource>
{

    private static ScheduledServiceMonthlyResource scheduledTask;

    @Override
    public ScheduledServiceMonthlyResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceMonthlyResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskOnce" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setStartDate( String.valueOf( startDate.getTime() ) );
            scheduledTask.setRecurringTime( "03:30" );
            scheduledTask.setRecurringDay( Arrays.asList( new String[] { "1", "9", "17", "25" } ) );

            scheduledTask.setTypeId( "org.sonatype.nexus.tasks.ReindexTask" );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceMonthlyResource scheduledTask )
    {
        scheduledTask.setRecurringTime( "00:00" );
    }

}
