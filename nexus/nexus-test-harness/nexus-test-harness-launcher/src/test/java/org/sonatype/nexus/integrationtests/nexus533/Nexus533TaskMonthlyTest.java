package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Nexus533TaskMonthlyTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceAdvancedResource>
{

    private static ScheduledServiceAdvancedResource scheduledTask;

    @Override
    public ScheduledServiceAdvancedResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceAdvancedResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskOnce" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setCronCommand( "0 0 12 ? * WED" );

            scheduledTask.setTypeId( "org.sonatype.nexus.tasks.ReindexTask" );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceAdvancedResource scheduledTask )
    {
        scheduledTask.setCronCommand( "0 0 12 ? * WED,FRI" );
    }

}
