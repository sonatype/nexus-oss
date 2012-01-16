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
package org.sonatype.nexus.integrationtests.nexus4066;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.scheduling.TaskState;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Check for tasks mutual exclusion (like two reindex tasks for same repository will run serialized, one will "win" and
 * run, one will "loose" and wait for winner to finish).
 */
public class Nexus4066TaskMutualExclusionIT
    extends AbstractNexusIntegrationTest
{

    /*
     * When last argument is false mean task should run in parallel. When it is true task should run serialized.
     */
    @DataProvider( name = "data", parallel = false )
    public Object[][] createData()
    {
        // GofG == group of groups
        return new Object[][] {//
        { "repo", "group", true },//
            { "repo", "repo2", false },//
            { "repo", "group2", false },//
            { "group", "group2", false },//
            { "repo2", "group", false },//
            { "repo2", "group2", true },//
            { "repo", "GofG", true },//
            { "group", "GofG", true },//
            { "repo2", "GofG", false },//
            { "group2", "GofG", false },//
            { "GofG2", "GofG", false },//
            { "repo2", "GofG2", true },//
            { "group2", "GofG2", true },//
            { "repo", "GofG2", false },//
            { "group", "GofG2", false },//
        };
    }

    private List<ScheduledServiceListResource> tasks;

    @BeforeMethod
    public void w8()
        throws Exception
    {
        tasks = Lists.newArrayList();

        TaskScheduleUtil.waitForAllTasksToStop();
    }

    @AfterMethod
    public void killTasks()
        throws IOException
    {
        // first I wanna cancel any blocked task, then I cancel the blocker
        Collections.reverse( tasks );

        for ( ScheduledServiceListResource task : tasks )
        {
            TaskScheduleUtil.cancel( task.getId() );
        }
    }

    @Test( dataProvider = "data" )
    public void run( String repo1, String repo2, boolean shouldWait )
        throws Exception
    {
        try
        {
            ScheduledServiceListResource task1 = createTask( repo1 );
            assertThat( task1.getStatus(), equalTo( TaskState.RUNNING.name() ) );

            ScheduledServiceListResource task2 = createTask( repo2 );
            if ( shouldWait )
            {
                assertThat( task2.getStatus(), equalTo( TaskState.SLEEPING.name() ) );
            }
            else
            {
                assertThat( task2.getStatus(), equalTo( TaskState.RUNNING.name() ) );
            }
        }
        catch ( java.lang.AssertionError e )
        {
            throw new RuntimeException( "Repo1: " + repo1 + " repo2: " + repo2 + " shouldWait: " + shouldWait, e );
        }
    }

    private ScheduledServiceListResource createTask( String repo )
        throws Exception
    {
        final String taskName = "SleepRepositoryTask_" + repo + "_" + System.nanoTime();
        TaskScheduleUtil.runTask( taskName, "SleepRepositoryTask", 0,
            TaskScheduleUtil.newProperty( "repositoryId", repo ),
            TaskScheduleUtil.newProperty( "time", String.valueOf( 50 ) ) );

        Thread.sleep( 2000 );

        ScheduledServiceListResource task = TaskScheduleUtil.getTask( taskName );

        tasks.add( task );

        return task;
    }

}
