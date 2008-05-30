package org.sonatype.scheduling;

import java.util.Date;
import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.scheduling.iterators.SchedulerIterator;

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

        SubmittedTask<Object> st = defaultScheduler.submit( tr );

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

        SubmittedTask<Integer> st = defaultScheduler.submit( tr );

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

    public void testSecondsRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        long nearFuture = System.currentTimeMillis() + 500;

        SchedulerIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        IteratingTask<Object> st = defaultScheduler.iterate( tr, iterator );

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

        SchedulerIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        IteratingTask<Integer> st = defaultScheduler.iterate( tr, iterator );

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

        SchedulerIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        IteratingTask<Object> st = defaultScheduler.iterate( tr, iterator );

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

        SchedulerIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        IteratingTask<Integer> st = defaultScheduler.iterate( tr, iterator );

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        st.cancel();

        assertEquals( 0, tr.getRunCount() );

        assertTrue( st.getTaskState().isEndingState() );

        assertEquals( TaskState.CANCELLED, st.getTaskState() );

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
