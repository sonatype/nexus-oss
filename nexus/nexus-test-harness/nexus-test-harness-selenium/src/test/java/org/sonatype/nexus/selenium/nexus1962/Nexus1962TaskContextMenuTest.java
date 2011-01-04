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
package org.sonatype.nexus.selenium.nexus1962;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.hamcrest.collection.IsCollectionContaining;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Component( role = Nexus1962TaskContextMenuTest.class )
public class Nexus1962TaskContextMenuTest
    extends SeleniumTest
{
    @Requirement
    private NexusScheduler nexusScheduler;

    private ScheduledTask<Object> task;

    @BeforeMethod
    public void createTask()
    {
        task =
            nexusScheduler.schedule( "selenium-context-task",
                                     nexusScheduler.createTaskInstance( EmptyTrashTask.class ), new ManualRunSchedule() );
    }

    @AfterMethod
    public void deleteTask()
    {
        if ( task != null )
        {
            try
            {
                task.get();
            }
            catch ( Exception e )
            {
                log.error( e.getMessage(), e );
            }
            task.cancel();
            task = null;
        }
    }

    @Test
    public void contextMenuRefresh()
        throws InterruptedException
    {
        doLogin();

        SchedulesConfigTab tasks = main.openTasks();
        ScheduleGrid scheduleGrid = tasks.getScheduleGrid();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

        // refresh
        tasks.contextMenuRefresh( 0 );
        assertThat( Arrays.asList( scheduleGrid.getKeys() ), IsCollectionContaining.hasItems( uiTaskId ) );
    }

    @Test
    public void contextMenuRun()
        throws InterruptedException
    {
        doLogin();
        SchedulesConfigTab tasks = main.openTasks();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

        // run error
        MockHelper.expect( "/schedule_run/{scheduledServiceId}", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST,
                                                                                   null ) );
        tasks.contextMenuRun( uiTaskId );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        // run
        MockHelper.listen( "/schedule_run/{scheduledServiceId}", new MockListener() );
        tasks.contextMenuRun( uiTaskId );

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();
    }

    @Test
    public void contextMenuDelete()
        throws InterruptedException
    {
        doLogin();

        SchedulesConfigTab tasks = main.openTasks();

        String uiTaskId = nexusBaseURL + "service/local/schedules/" + task.getId();

        // delete
        tasks.contextMenuDelete( uiTaskId );

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        try
        {
            nexusScheduler.getTaskById( task.getId() );
            Assert.fail( "Task shouldn't exists! " + task.getId() );
        }
        catch ( NoSuchTaskException e )
        {
            // expected
        }

    }
}
