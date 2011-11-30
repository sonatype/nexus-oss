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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;
import static org.sonatype.sisu.goodies.common.Time.time;
import static org.sonatype.tests.http.server.fluent.Behaviours.error;
import static org.sonatype.tests.http.server.fluent.Behaviours.pause;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import org.restlet.data.Status;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.DownloadIndexesTaskDescriptor;
import org.sonatype.nexus.test.utils.NexusRequestMatchers;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.sisu.goodies.common.Time;
import org.sonatype.tests.http.server.fluent.Server;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus4341RunningTaskNotEditableIT
    extends AbstractNexusProxyIntegrationTest
{

    private Server return500Server;


    @BeforeMethod(alwaysRun = true)
    public void replaceServer()
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE );
        server.stop();
        int port = server.getPort();

        return500Server =
            Server.withPort( port ).serve( "/*" ).withBehaviours( pause( time( 10, TimeUnit.SECONDS ) ), error( 500 ) ).start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopServer()
        throws Exception
    {
        if ( return500Server != null )
        {
            return500Server.stop();
        }
    }

    private void createDownloadIndexesTask( String name )
        throws Exception
    {
        ScheduledServiceBaseResource scheduledTask = getScheduledTaskTemplate( name );

        TaskScheduleUtil.create( scheduledTask, isSuccessful() );
    }

    private void verifyNoUpdate( ScheduledServiceListResource resource )
        throws IOException
    {
        log.info( "Trying to update {} ({})", resource.getName(), resource.getStatus() );

        ScheduledServiceBaseResource changed = getScheduledTaskTemplate( "changed" );
        changed.setId( resource.getId() );
        Status status = TaskScheduleUtil.update( changed );
        assertThat( "Should not have been able to update task with state " + resource.getStatus() + ", "
                        + status.getDescription(), status, isClientError() );
    }

    private ScheduledServiceBaseResource getScheduledTaskTemplate( String name )
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setKey( DownloadIndexesTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        repositoryProp.setValue( getTestRepositoryId() );

        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( name );
        scheduledTask.setTypeId( DownloadIndexesTaskDescriptor.ID );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.addProperty( repositoryProp );
        return scheduledTask;
    }

    @Test
    public void testNoUpdateForRunningTasks()
        throws Exception
    {
        createDownloadIndexesTask( "Nexus4341Task1" );
        createDownloadIndexesTask( "Nexus4341Task2" );

        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();

        assertThat( tasks, hasSize( 2 ) );

        for ( ScheduledServiceListResource resource : tasks )
        {
            log.info( "Starting task: {}", resource.getName() );
            Status status = TaskScheduleUtil.run( resource.getId() );
            Assert.assertTrue( status.isSuccess() );
        }

        int ticks = 1;
        while ( ticks <= 60 )
        {
            tasks = TaskScheduleUtil.getTasks();
            assertThat( tasks, hasSize( 2 ) );
            if ( tasks.get( 0 ).getStatus().equals( "RUNNING" ) && tasks.get( 1 ).getStatus().equals( "SLEEPING" ) )
            {
                break;
            }
            else if ( tasks.get( 1 ).getStatus().equals( "RUNNING" ) && tasks.get( 0 ).getStatus().equals( "SLEEPING" ) )
            {
                break;
            }
            
            ticks++;
            Thread.yield();
            Time.seconds( 1 ).sleep();
        }

        assertThat( "did not find RUNNING/SLEEPING state for two tasks in 60s", ticks, lessThanOrEqualTo( 60 ) );

        tasks = TaskScheduleUtil.getTasks();
        assertThat( tasks, hasSize( 2 ) );

        Set<String> seenStates = Sets.newHashSet();

        for ( ScheduledServiceListResource task : tasks )
        {
            seenStates.add( task.getStatus() );
            log.info( "Found task {} with state {}", task.getName(), task.getStatus() );
            if ( task.getStatus().equals( "SLEEPING" ) )
            {
                verifyNoUpdate( task );
                TaskScheduleUtil.cancel( task.getId() );
            }
            else if ( task.getStatus().equals( "RUNNING" ) )
            {
                verifyNoUpdate( task );

                log.info( "Canceling running task and trying to update" );
                TaskScheduleUtil.cancel( task.getId() );
                task = TaskScheduleUtil.getTask( task.getName() );

                seenStates.add( task.getStatus() );

                verifyNoUpdate( task );
            }
        }

        assertThat( seenStates, containsInAnyOrder( "SLEEPING", "RUNNING", "CANCELLING" ) );
    }
}
