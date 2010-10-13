/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus810;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus810PackageNamesInNexusConfIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @Test
    public void checkNexusConfForPackageNames()
        throws Exception
    {

        // create a task
        ScheduledServiceAdvancedResource scheduledTask = new ScheduledServiceAdvancedResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( "taskAdvanced" );
        scheduledTask.setSchedule( "advanced" );
        // A future date
        Date startDate = DateUtils.addDays( new Date(), 10 );
        startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
        scheduledTask.setCronCommand( "0 0 12 ? * WED" );

        scheduledTask.setTypeId( ReindexTaskDescriptor.ID );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryOrGroupId" );
        prop.setValue( "all_repo" );
        scheduledTask.addProperty( prop );

        Assert.assertTrue( TaskScheduleUtil.create( scheduledTask ).isSuccess(), "Expected task to be created: " );
        
        // now check the conf
        List<CScheduledTask> tasks = getNexusConfigUtil().getNexusConfig().getTasks();
        Assert.assertTrue( tasks.size() > 0, "Expected at least 1 task in nexus.xml" );
        
        for ( CScheduledTask task : tasks )
        {
            Assert.assertFalse( task.getType().contains( "org.sonatype." ), "Found package name in nexus.xml for task type: "+ task.getType());
        }

    }
}
