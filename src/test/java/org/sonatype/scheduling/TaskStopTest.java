package org.sonatype.scheduling;

import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.scheduling.schedules.ManualRunSchedule;

public class TaskStopTest
    extends PlexusTestCase
{
    protected DefaultScheduler defaultScheduler;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );
    }

    public void testStopTask()
        throws Exception
    {
        RunForeverCallable callable = new RunForeverCallable();

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.submit( "Test Task", callable );

        assertFalse( callable.isAllDone() );

        // Give task a chance to get going for a bit
        callable.blockForStart();

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );

        assertEquals( TaskState.RUNNING, task.getTaskState() );

        task.cancel( true );

        callable.blockForDone();

        // Now check and see if task is still running...
        assertTrue( callable.isAllDone() );
    }

    public void testCancelOnlyWaitForFinishExecution()
        throws Exception
    {
        RunForeverCallable callable = new RunForeverCallable( 500 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.submit( "Test Task", callable );

        assertFalse( callable.isAllDone() );

        callable.blockForStart();

        assertEquals( 1, defaultScheduler.getAllTasks().size() );

        assertEquals( TaskState.RUNNING, task.getTaskState() );

        task.cancelOnly();

        assertFalse( "task was killed immediately", callable.isAllDone() );
        assertFalse( "running task was eagerly removed", defaultScheduler.getAllTasks().isEmpty() );

        callable.blockForDone();

        assertTrue( "task was not done", callable.isAllDone() );
        assertTrue( "task not removed", defaultScheduler.getAllTasks().isEmpty() );
    }

    public void testCancelDoesNotRemoveRunningTask()
        throws Exception
    {
        RunForeverCallable callable = new RunForeverCallable( 500 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.submit( "Test Task", callable );

        assertFalse( callable.isAllDone() );

        callable.blockForStart();

        assertEquals( 1, defaultScheduler.getAllTasks().size() );

        assertEquals( TaskState.RUNNING, task.getTaskState() );

        task.cancel();

        assertFalse( "task was killed immediately", callable.isAllDone() );
        assertFalse( "running task was eagerly removed", defaultScheduler.getAllTasks().isEmpty() );

        callable.blockForDone();

        assertTrue( "task was not done", callable.isAllDone() );
        assertTrue( "task not removed", defaultScheduler.getAllTasks().isEmpty() );
    }

    public void testCancelRemovesIdleTask()
    {
        RunForeverCallable callable = new RunForeverCallable( 500 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", callable, new ManualRunSchedule() );

        assertFalse( callable.isAllDone() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );

        assertEquals( TaskState.SUBMITTED, task.getTaskState() );

        task.cancel();

        assertTrue( "idle task was not removed", defaultScheduler.getAllTasks().isEmpty() );
        assertFalse( "task was killed immediately", callable.isAllDone() );
    }

    public class RunForeverCallable
        implements Callable<Integer>
    {
        private boolean allDone = false;

        private int runTicks;

        public RunForeverCallable()
        {
            this.runTicks = Integer.MAX_VALUE;
        }

        public RunForeverCallable( int ticks )
        {
            this.runTicks = ticks;
        }

        private boolean started = false;

        public Integer call()
            throws Exception
        {
            try
            {
                int ticks = 0;
                while ( ticks++ < runTicks )
                {
                    // Replace with Thread.yield() to see the problem. The sleep state will
                    // cause the thread to stop
                    Thread.sleep( 1 );
                    started = true;
                }
                System.out.println( "done running" );
            }
            finally
            {
                allDone = true;
            }

            return null;
        }

        public boolean isAllDone()
        {
            return allDone;
        }

        public void blockForStart()
            throws Exception
        {
            while ( started == false )
            {
                Thread.sleep( 10 );
            }
        }

        public void blockForDone()
            throws Exception
        {
            while ( allDone == false )
            {
                Thread.sleep( 10 );
            }
        }
    }

}
