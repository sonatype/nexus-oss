/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.scheduling.iterators.DailySchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultSchedulerTest
    extends AbstractNexusTestCase
{
    protected DefaultScheduler defaultScheduler;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );

        defaultScheduler.startService();
    }

    public void tearDown()
        throws Exception
    {
        defaultScheduler.stopService();

        super.tearDown();
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

        ScheduledTask<Integer> st = defaultScheduler.store( "default", tr, null );

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

        ScheduledTask<Object> st = defaultScheduler.schedule( "default", tr, schedule, null, true );

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

        ScheduledTask<Integer> st = defaultScheduler.schedule( "default", tr, schedule, null, true );

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

        ScheduledTask<Object> st = defaultScheduler.schedule( "default", tr, schedule, null, true );

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

        ScheduledTask<Integer> st = defaultScheduler.schedule( "default", tr, schedule, null, true );

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
