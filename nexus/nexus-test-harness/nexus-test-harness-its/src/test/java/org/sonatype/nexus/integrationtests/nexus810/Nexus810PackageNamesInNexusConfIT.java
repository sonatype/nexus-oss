/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
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

        scheduledTask.setTypeId( UpdateIndexTaskDescriptor.ID );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
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
