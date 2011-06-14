package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;

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

        assertEquals( TaskState.CANCELLING, task.getTaskState() );

        assertFalse( "task was killed immediately", callable.isAllDone() );
        assertFalse( "running task was eagerly removed", defaultScheduler.getAllTasks().isEmpty() );

        callable.blockForDone();

        // let state machine catch up
        Thread.sleep( 100 );

        assertEquals( TaskState.CANCELLED, task.getTaskState() );

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

        assertEquals( TaskState.CANCELLING, task.getTaskState() );

        assertFalse( "task was killed immediately", callable.isAllDone() );
        assertFalse( "running task was eagerly removed", defaultScheduler.getAllTasks().isEmpty() );

        callable.blockForDone();

        // let state machine catch up
        Thread.sleep( 100 );

        assertEquals( TaskState.CANCELLED, task.getTaskState() );

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

    public void testCancelledRunningTaskWithScheduleIsRemovedLater()
        throws Exception
    {
        RunForeverCallable callable = new RunForeverCallable( 500 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", callable, new ManualRunSchedule() );

        assertFalse( callable.isAllDone() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );

        assertEquals( TaskState.SUBMITTED, task.getTaskState() );

        task.runNow();

        callable.blockForStart();

        assertEquals( TaskState.RUNNING, task.getTaskState() );

        task.cancel();

        assertEquals( TaskState.CANCELLING, task.getTaskState() );

        callable.blockForDone();

        // let state machine catch up
        Thread.sleep( 100 );

        assertEquals( TaskState.CANCELLED, task.getTaskState() );

        // let task finish call()
        Thread.sleep( 500 );

        assertTrue( "task was not removed", defaultScheduler.getAllTasks().isEmpty() );
        assertTrue( "task was killed immediately", callable.isAllDone() );

    }

    public void testCancelRemovesBlockedOneShotTasks()
        throws Exception
    {
        RunForeverTask callable = new RunForeverTask( 5000 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", callable, new RunNowSchedule() );

        callable.blockForStart();

        RunForeverTask blockedCallable = new RunForeverTask( 5000 );
        ScheduledTask<Integer> blockedTask =
            defaultScheduler.schedule( "Blocked Task", blockedCallable, new RunNowSchedule() );

        // let scheduler catch up
        Thread.sleep( 600 );

        assertEquals( TaskState.SLEEPING, blockedTask.getTaskState() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancelOnly();

        assertEquals( TaskState.CANCELLED, blockedTask.getTaskState() );
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 1, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        task.cancel( true );
        callable.blockForDone();
        Thread.sleep( 50 );
        assertEquals( 0, defaultScheduler.getAllTasks().size() );
    }

    public void testCancelReschedulesBlockedTasks()
        throws Exception
    {
        RunForeverTask callable = new RunForeverTask( 3000 );

        assertFalse( callable.isAllDone() );

        ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", callable, new RunNowSchedule() );

        // let scheduler catch up
        Thread.sleep( 500 );

        RunForeverTask blockedCallable = new RunForeverTask( 3000 );
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.WEEK_OF_YEAR, 1 );
        ScheduledTask<Integer> blockedTask =
            defaultScheduler.schedule( "Blocked Task", blockedCallable, new DailySchedule( new Date(), cal.getTime() ) );

        blockedTask.runNow();

        // let scheduler catch up
        Thread.sleep( 500 );

        assertEquals( TaskState.SLEEPING, blockedTask.getTaskState() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancelOnly();

        assertEquals( TaskState.WAITING, blockedTask.getTaskState() );
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );
        assertFalse( blockedTask.getScheduleIterator().isFinished() );

        task.cancel( true );
        callable.blockForDone();

        assertEquals( TaskState.WAITING, blockedTask.getTaskState() );
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 1, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancel();
        Thread.sleep( 50 );
        assertEquals( 0, defaultScheduler.getAllTasks().size() );
    }

    public void testCancellingStateBlocksTasks()
        throws Exception
    {
        RunForeverTask callable = new RunForeverTask( 2000 );

        assertFalse( callable.isAllDone() );
        assertEquals( 0, defaultScheduler.getAllTasks().size() );

        ScheduledTask<Integer> task = defaultScheduler.submit( "Test Task", callable );

        callable.blockForStart();

        task.cancelOnly();
        assertEquals( TaskState.CANCELLING, task.getTaskState() );

        RunForeverTask blockedCallable = new RunForeverTask( 5000 );
        ScheduledTask<Integer> blockedTask =
            defaultScheduler.schedule( "Blocked Task", blockedCallable, new RunNowSchedule() );

        // let scheduler catch up
        Thread.sleep( 600 );

        assertFalse( blockedCallable.isStarted() );
        assertEquals( TaskState.SLEEPING, blockedTask.getTaskState() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancel();
        assertFalse( blockedCallable.isStarted() );

        assertEquals( TaskState.CANCELLED, blockedTask.getTaskState() );
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 1, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        // task is already cancelled without interruption, so we have to wait for normal completion
        // task.cancel( true );

        callable.blockForDone();

        assertEquals( 0, defaultScheduler.getAllTasks().size() );
    }

    public void testCancelBlockedTask()
        throws Exception
    {
        RunForeverTask callable = new RunForeverTask();

        assertFalse( callable.isAllDone() );
        assertEquals( 0, defaultScheduler.getAllTasks().size() );

        final ScheduledTask<Integer> task = defaultScheduler.submit( "Test Task", callable );

        callable.blockForStart();

        final RunForeverTask blockedCallable = new RunForeverTask();
        final ScheduledTask<Integer> blockedTask =
            defaultScheduler.schedule( "Blocked Task", blockedCallable, new ManualRunSchedule() );

        Runnable runCancelBlockedTask = new Runnable()
        {

            public void run()
            {

                blockedTask.runNow();

                // let scheduler catch up
                try
                {
                    Thread.sleep( 600 );
                }
                catch ( InterruptedException e )
                {
                    throw new IllegalStateException( e );
                }

                assertFalse( blockedCallable.isStarted() );
                assertEquals( TaskState.SLEEPING, blockedTask.getTaskState() );

                assertEquals( 1, defaultScheduler.getAllTasks().size() );
                assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

                blockedTask.cancelOnly();

                assertFalse( blockedCallable.isStarted() );

                assertEquals( TaskState.SUBMITTED, blockedTask.getTaskState() );
                assertEquals( 1, defaultScheduler.getAllTasks().size() );
                assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );
            }
        };

        runCancelBlockedTask.run();
        runCancelBlockedTask.run();
        runCancelBlockedTask.run();

        task.cancel( true );
        blockedTask.cancel( true );

        callable.blockForDone();

        assertEquals( 0, defaultScheduler.getAllTasks().size() );
    }

    public void testCancelManualRunStateIsSubmitted()
        throws Exception
    {
        RunForeverTask callable = new RunForeverTask( 2000 );

        assertFalse( callable.isAllDone() );
        assertEquals( 0, defaultScheduler.getAllTasks().size() );

        final ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", callable, new ManualRunSchedule() );

        task.runNow();

        callable.blockForStart();

        final RunForeverTask blockedCallable = new RunForeverTask( 2000 );
        final ScheduledTask<Integer> blockedTask =
            defaultScheduler.schedule( "Blocked Task", blockedCallable, new ManualRunSchedule() );

        blockedTask.runNow();

        Thread.sleep( 600 );

        assertFalse( blockedCallable.isStarted() );
        assertEquals( TaskState.SLEEPING, blockedTask.getTaskState() );

        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancelOnly();

        assertFalse( blockedCallable.isStarted() );

        assertEquals( TaskState.SUBMITTED, blockedTask.getTaskState() );
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        assertEquals( 2, defaultScheduler.getAllTasks().get( task.getType() ).size() );

        blockedTask.cancel();

        task.cancelOnly();
        callable.blockForDone();

        assertEquals( TaskState.SUBMITTED, task.getTaskState() );

        task.cancel();

        assertEquals( 0, defaultScheduler.getAllTasks().size() );
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

        public boolean isStarted()
        {
            return started;
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

    public class RunForeverTask
        extends RunForeverCallable
        implements SchedulerTask<Integer>
    {

        public RunForeverTask()
        {
            super();
        }

        public RunForeverTask( int i )
        {
            super( i );
        }

        public boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> currentActiveTasks )
        {
            return true;
        }

        public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> currentActiveTasks )
        {
            for ( List<ScheduledTask<?>> list : currentActiveTasks.values() )
            {
                for ( ScheduledTask<?> task : list )
                {
                    if ( task.getTaskState().isExecuting() )
                    {
                        System.out.println( "concurrent execution not allowed" );
                        return false;
                    }
                }
            }
            return true;
        }

        public void addParameter( String key, String value )
        {
        }

        public String getParameter( String key )
        {
            return null;
        }

        public Map<String, String> getParameters()
        {
            return null;
        }

    }

}
