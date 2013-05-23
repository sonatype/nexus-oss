/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.*;

public class RunNowSchedulerTest
    extends TestSupport
{
    protected DefaultScheduler defaultScheduler;

    @Before
    public void setUp()
        throws Exception
    {
        defaultScheduler = new DefaultScheduler(new SimpleTaskConfigManager());
    }

    @Test
    public void testRunNowRunnable()
        throws Exception
    {
        TestRunnable tr = new TestRunnable();

        ScheduledTask<Object> st = defaultScheduler.submit( "default", tr );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertNull( st.getNextRun() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    @Test
    public void testRunNowCallable()
        throws Exception
    {
        TestCallable tr = new TestCallable();

        ScheduledTask<Integer> st = defaultScheduler.submit( "default", tr );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );

        assertEquals( 1, st.getResults().size() );

        assertEquals( Integer.valueOf( 0 ), st.getResults().get( 0 ) );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertNull( st.getNextRun() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    // Helper classes

    public class TestRunnable
        implements Runnable
    {
        private int runCount = 0;

        public void run()
        {
            runCount++;
        }

        public int getRunCount()
        {
            return runCount;
        }
    }

    public class TestCallable
        implements Callable<Integer>
    {
        private int runCount = 0;

        public Integer call()
            throws Exception
        {
            return runCount++;
        }

        public int getRunCount()
        {
            return runCount;
        }
    }

}
