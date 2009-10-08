package org.sonatype.nexus.integrationtests.nexus2797;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus2797Validate140TaskUpgrade
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validateTaskHasNextRunDate()
        throws Exception
    {
        doIt();
        
        // now stop and restart nexus, make sure still ok
        NexusStatusUtil.stop();
        NexusStatusUtil.start();
        
        doIt();
    }
    
    private void doIt()
        throws Exception
    {
        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();
        
        Assert.assertEquals( 3, tasks.size() );
        
        Date lastRunTime = new Date( 1111111111131l );
        
        // not quite sure why, but we add 20 ms to the last run time when calling
        // setLastRun in DefaultScheduledTask
        Assert.assertEquals( lastRunTime.toString(), tasks.get( 0 ).getLastRunTime() );
        Assert.assertEquals( lastRunTime.toString(), tasks.get( 1 ).getLastRunTime() );
        Assert.assertEquals( lastRunTime.toString(), tasks.get( 2 ).getLastRunTime() );
        
        Date nextRunTime = fixNextRunTime( new Date( 1230777000000l ) );
        
        //problem was simply that next run time was invalidly calculated, and never set
        //we simply want to make sure it is set
        Assert.assertEquals( nextRunTime.toString(), tasks.get( 0 ).getNextRunTime() );
        Assert.assertEquals( nextRunTime.toString(), tasks.get( 1 ).getNextRunTime() );
        Assert.assertEquals( nextRunTime.toString(), tasks.get( 2 ).getNextRunTime() );
    }
    
    private Date fixNextRunTime( Date nextRunTime )
    {
        Calendar now = Calendar.getInstance();
        
        Calendar cal = Calendar.getInstance();
        
        cal.setTime( nextRunTime );
        
        cal.set( Calendar.YEAR, now.get( Calendar.YEAR ) );
        cal.set( Calendar.DAY_OF_YEAR, now.get( Calendar.DAY_OF_YEAR ) );
        
        if ( cal.before( now ) )
        {
            cal.add( Calendar.DAY_OF_YEAR, 1 );
}
        
        return cal.getTime();
    }
}
