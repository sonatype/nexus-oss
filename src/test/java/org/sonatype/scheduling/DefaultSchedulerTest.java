/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.scheduling.iterators.DailySchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultSchedulerTest
    extends PlexusTestCase
{
    protected DefaultScheduler defaultScheduler;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );
    }

    public void testSimpleRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        ScheduledTask<Object> st = defaultScheduler.submit( "default", tr, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    public void testSimpleCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        ScheduledTask<Integer> st = defaultScheduler.submit( "default", tr, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );

        assertEquals( Integer.valueOf( 0 ), st.getIfDone() );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    public void testManual()
        throws Exception
    {
        TestCallable tr = new TestCallable();

        ScheduledTask<Integer> st = defaultScheduler.schedule( "default", tr, new ManualRunSchedule(), null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        // Give the scheduler a chance to start if it would (it shouldn't that's the test)
        Thread.sleep( 100 );

        assertEquals( TaskState.SUBMITTED, st.getTaskState() );

        st.runNow();

        // Give the task a chance to start
        Thread.sleep( 100 );

        // Now wait for it to finish
        while ( !st.getTaskState().equals( TaskState.SUBMITTED ) )
        {
            Thread.sleep( 100 );
        }

        assertEquals( 1, tr.getRunCount() );

        assertEquals( TaskState.SUBMITTED, st.getTaskState() );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        st.cancel();

        while ( defaultScheduler.getActiveTasks().size() > 0 )
        {
            Thread.sleep( 100 );
        }
    }

    public void testSecondsRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        long nearFuture = System.currentTimeMillis() + 500;

        Schedule schedule = getEverySecondSchedule( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        ScheduledTask<Object> st = defaultScheduler.schedule( "default", tr, schedule, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 5, tr.getRunCount() );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    public void testSecondsCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        long nearFuture = System.currentTimeMillis() + 500;

        Schedule schedule = getEverySecondSchedule( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        ScheduledTask<Integer> st = defaultScheduler.schedule( "default", tr, schedule, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        while ( !st.getTaskState().isEndingState() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 5, tr.getRunCount() );

        assertEquals( 5, st.getResults().size() );

        assertEquals( Integer.valueOf( 0 ), st.getResults().get( 0 ) );

        assertEquals( Integer.valueOf( 1 ), st.getResults().get( 1 ) );

        assertEquals( Integer.valueOf( 2 ), st.getResults().get( 2 ) );

        assertEquals( Integer.valueOf( 3 ), st.getResults().get( 3 ) );

        assertEquals( Integer.valueOf( 4 ), st.getResults().get( 4 ) );

        assertEquals( TaskState.FINISHED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    public void testCancelRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        long nearFuture = System.currentTimeMillis() + 500;

        Schedule schedule = getEverySecondSchedule( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        ScheduledTask<Object> st = defaultScheduler.schedule( "default", tr, schedule, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        st.cancel();

        assertEquals( 0, tr.getRunCount() );

        assertTrue( st.getTaskState().isEndingState() );

        assertEquals( TaskState.CANCELLED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    public void testCancelCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        long nearFuture = System.currentTimeMillis() + 500;

        Schedule schedule = getEverySecondSchedule( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        ScheduledTask<Integer> st = defaultScheduler.schedule( "default", tr, schedule, null );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        st.cancel();

        assertEquals( 0, tr.getRunCount() );

        assertTrue( st.getTaskState().isEndingState() );

        assertEquals( TaskState.CANCELLED, st.getTaskState() );

        assertEquals( 0, defaultScheduler.getActiveTasks().size() );
    }

    protected Schedule getEverySecondSchedule( Date start, Date stop )
    {
        return new SecondScheduler( start, stop );
    }

    // Helper classes

    public class SecondScheduler
        extends DailySchedule
    {
        public SecondScheduler( Date startDate, Date endDate )
        {
            super( startDate, endDate );
        }

        protected SchedulerIterator createIterator()
        {
            return new SecondSchedulerIterator( getStartDate(), getEndDate() );
        }
    }

    public class SecondSchedulerIterator
        extends DailySchedulerIterator
    {
        public SecondSchedulerIterator( Date startingDate, Date endingDate )
        {
            super( startingDate, endingDate );
        }

        public void stepNext()
        {
            getCalendar().add( Calendar.SECOND, 1 );
        }
    }

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
