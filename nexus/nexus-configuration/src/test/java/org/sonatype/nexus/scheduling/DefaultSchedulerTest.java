package org.sonatype.nexus.scheduling;

import java.util.Date;
import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;

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

        SubmittedTask st = defaultScheduler.submit( tr );

        while ( !st.isDone() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );
    }

    public void testSimpleCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        SubmittedTask st = defaultScheduler.submit( tr );

        while ( !st.isDone() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 1, tr.getRunCount() );
    }

    public void testSecondsRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        long nearFuture = System.currentTimeMillis() + 500;

        ScheduleIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        SubmittedTask st = defaultScheduler.schedule( tr, iterator );

        while ( !st.isDone() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 5, tr.getRunCount() );
    }

    public void testSecondsCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        long nearFuture = System.currentTimeMillis() + 500;

        ScheduleIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        SubmittedTask st = defaultScheduler.schedule( tr, iterator );

        while ( !st.isDone() )
        {
            Thread.sleep( 300 );
        }

        assertEquals( 5, tr.getRunCount() );
    }

    public void testCancelRunnable()
        throws Exception
    {
        TestRunnable tr = null;

        tr = new TestRunnable();

        long nearFuture = System.currentTimeMillis() + 500;

        ScheduleIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        SubmittedTask st = defaultScheduler.schedule( tr, iterator );

        st.cancel();

        assertEquals( 0, tr.getRunCount() );

        assertTrue( st.isCancelled() );

        assertTrue( st.isDone() );
    }

    public void testCancelCallable()
        throws Exception
    {
        TestCallable tr = null;

        tr = new TestCallable();

        long nearFuture = System.currentTimeMillis() + 500;

        ScheduleIterator iterator = new SecondScheduleIterator( new Date( nearFuture ), new Date( nearFuture + 4900 ) );

        SubmittedTask st = defaultScheduler.schedule( tr, iterator );

        st.cancel();

        assertEquals( 0, tr.getRunCount() );

        assertTrue( st.isCancelled() );

        assertTrue( st.isDone() );
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
