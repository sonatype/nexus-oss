package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Nexus533TaskWeeklyTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceWeeklyResource>
{

    private static ScheduledServiceWeeklyResource scheduledTask;

    @Override
    public ScheduledServiceWeeklyResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceWeeklyResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskWeekly" );
            scheduledTask.setSchedule( "weekly" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setStartDate( String.valueOf( startDate.getTime() ) );
            scheduledTask.setRecurringTime( "03:30" );
            scheduledTask.setRecurringDay( Arrays.asList( new String[] { "monday", "wednesday", "friday" } ) );

            scheduledTask.setTypeId( "org.sonatype.nexus.tasks.ReindexTask" );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceWeeklyResource scheduledTask )
    {
        scheduledTask.setRecurringTime( "00:00" );
    }

}
