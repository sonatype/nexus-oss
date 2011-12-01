/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4341;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isClientError;

import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.sisu.goodies.common.Time;
import org.testng.annotations.Test;

public class Nexus4341RunningTaskNotEditableIT
    extends AbstractNexusIntegrationTest
{

    private ScheduledServiceListResource createTask()
        throws Exception
    {
        final String taskName = "SleepRepositoryTask_" + getTestRepositoryId() + "_" + System.nanoTime();
        TaskScheduleUtil.runTask( taskName, "SleepRepositoryTask", 0,
                                  TaskScheduleUtil.newProperty( "repositoryId", getTestRepositoryId() ),
                                  TaskScheduleUtil.newProperty( "time", String.valueOf( 10 ) ) );

        Time.seconds( 1 ).sleep();

        return TaskScheduleUtil.getTask( taskName );
    }

    private void verifyNoUpdate( ScheduledServiceListResource resource )
        throws IOException
    {
        log.info( "Trying to update {} ({})", resource.getName(), resource.getStatus() );

        ScheduledServiceBaseResource changed = new ScheduledServiceBaseResource();
        changed.setEnabled( true );
        changed.setId( resource.getId() );
        changed.setName( "otherName" );
        changed.setTypeId( resource.getTypeId() );
        changed.setSchedule( resource.getSchedule() );
        changed.addProperty( TaskScheduleUtil.newProperty( "repositoryId", getTestRepositoryId() ) );
        changed.addProperty( TaskScheduleUtil.newProperty( "time", String.valueOf( 10 ) ) );

        Status status = TaskScheduleUtil.update( changed );

        assertThat( "Should not have been able to update task with state " + resource.getStatus() + ", "
                        + status.getDescription(), status, isClientError() );
    }

    @Test
    public void testNoUpdateForRunningTasks()
        throws Exception
    {
        ScheduledServiceListResource running = createTask();
        ScheduledServiceListResource sleeping = createTask();

        assertThat( running.getStatus(), is( "RUNNING" ) );
        assertThat( sleeping.getStatus(), is( "SLEEPING" ) );

        verifyNoUpdate( sleeping );
        TaskScheduleUtil.cancel( sleeping.getId() );

        verifyNoUpdate( running );
        TaskScheduleUtil.cancel( running.getId() );

        ScheduledServiceListResource cancelled = TaskScheduleUtil.getTask( running.getName() );
        assertThat( cancelled.getStatus(), is( "CANCELLING" ) );
        verifyNoUpdate( cancelled );
    }
}
