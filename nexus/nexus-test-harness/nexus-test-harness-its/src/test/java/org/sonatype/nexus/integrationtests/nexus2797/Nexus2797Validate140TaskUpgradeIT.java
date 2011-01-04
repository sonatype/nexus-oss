/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus2797;

import java.util.Calendar;
import java.util.Date;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2797Validate140TaskUpgradeIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validateTaskHasNextRunDate()
        throws Exception
    {
        doIt();
        
        // now stop and restart nexus, make sure still ok
        restartNexus();
        
        doIt();
    }
    
    private void doIt()
        throws Exception
    {
        // not quite sure why, but we add 20 ms to the last run time when calling
        // setLastRun in DefaultScheduledTask
        Date lastRunTime = new Date( 1111111111131l );
        
        Assert.assertEquals( lastRunTime.toString(), TaskScheduleUtil.getTask( "task1" ).getLastRunTime() );
        Assert.assertEquals( lastRunTime.toString(), TaskScheduleUtil.getTask( "task2" ).getLastRunTime() );
        Assert.assertEquals( lastRunTime.toString(), TaskScheduleUtil.getTask( "task3" ).getLastRunTime() );
        Assert.assertEquals( "n/a", TaskScheduleUtil.getTask( "task4" ).getLastRunTime() );
        
        //problem was simply that next run time was invalidly calculated, and never set
        //we simply want to make sure it is set
        //we need to fix the next run time, as it will change depending on current date
        Date nextRunTime = fixNextRunTime( new Date( 1230777000000l ) );
        
        Assert.assertEquals( nextRunTime.toString(), TaskScheduleUtil.getTask( "task1" ).getNextRunTime() );
        Assert.assertEquals( nextRunTime.toString(), TaskScheduleUtil.getTask( "task2" ).getNextRunTime() );
        Assert.assertEquals( nextRunTime.toString(), TaskScheduleUtil.getTask( "task3" ).getNextRunTime() );
        Assert.assertEquals( nextRunTime.toString(), TaskScheduleUtil.getTask( "task4" ).getNextRunTime() );
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
