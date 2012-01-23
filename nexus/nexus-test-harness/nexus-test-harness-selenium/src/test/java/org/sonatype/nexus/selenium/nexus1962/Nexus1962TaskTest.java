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
package org.sonatype.nexus.selenium.nexus1962;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.hamcrest.CoreMatchers;
import org.restlet.data.Method;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ScheduleGrid;
import org.sonatype.nexus.mock.pages.SchedulesConfigFormTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus1962TaskTest.class )
public class Nexus1962TaskTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        SchedulesConfigFormTab newTask = scheduleGrid.newTask();

        NxAssert.requiredField( newTask.getName(), "taskname" );
        NxAssert.requiredField( newTask.getTaskType(), 0 );
        NxAssert.requiredField( newTask.getRecurrence(), 0 );

        newTask.cancel();
    }

    @Test
    public void crudTask()
        throws InterruptedException
    {
        doLogin();

        ScheduleGrid scheduleGrid = main.openTasks().getScheduleGrid();
        // CREATE
        MockListener ml = MockHelper.listen( "/schedules", new MockListener()
        {
            @Override
            public void onPayload( Object payload, MockEvent evt )
            {
                if ( evt.getMethod().equals( Method.POST ) )
                {
                    assertNotNull( payload );
                }
                else
                {
                    evt.block();
                }
            }
        } );

        SchedulesConfigFormTab taskForm = scheduleGrid.newTask();
        taskForm.populate( true, "seleniumTask", "EmptyTrashTask", "Manual" ).save();

        ScheduledServiceResourceStatusResponse task = (ScheduledServiceResourceStatusResponse) ml.getResult();
        assertNotNull( task );
        String taskId = task.getData().getResource().getId();
        String uiTaskId = nexusBaseURL + "service/local/schedules/" + taskId;

        scheduleGrid.refresh();
        assertTrue( scheduleGrid.contains( uiTaskId ) );

        // READ
        scheduleGrid.refresh().select( uiTaskId );
        assertEquals( "seleniumTask", taskForm.getName().getValue() );
        assertEquals( "EmptyTrashTask", taskForm.getTaskType().getValue() );
        assertEquals( "Manual", taskForm.getRecurrence().getValue() );

        // UPDATE
        taskForm.getName().type( "seleniumTaskUpdated" );
        taskForm.save();
        scheduleGrid.refresh().select( uiTaskId );
        assertThat( taskForm.getName().getValue(), CoreMatchers.equalTo( "seleniumTaskUpdated" ) );

        // DELETE
        scheduleGrid.deleteTask().clickYes();

        Assert.assertFalse( scheduleGrid.contains( uiTaskId ) );
    }

}
