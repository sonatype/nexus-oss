package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class DisabledScheduledTaskTest
    extends PlexusTestCase
{
    protected DefaultScheduler defaultScheduler;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );
    }

    public void testRunDisabledTaske()
        throws Exception
    {
         ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", new TestIntegerCallable(), this.getTestSchedule( 0 ) );
       task.setEnabled( false );
         
        // manually run the task
        task.runNow();

        assertEquals( 1, defaultScheduler.getActiveTasks().size() );
        while ( task.getLastRun() == null )
        {
            Thread.sleep( 300 );
        }

         assertEquals( TaskState.WAITING, task.getTaskState() ); // if task is disabled

        assertEquals( 1, task.getResults().get( 0 ).intValue() );

        assertNotNull( task.getNextRun() );
        
        // make sure the task is still disabled
        assertFalse(task.isEnabled());
        
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
    }
    
    public void testDisabledTaskOnSchedule()
        throws Exception
    {
        ScheduledTask<Integer> task = defaultScheduler.schedule( "Test Task", new TestIntegerCallable(), this.getTestSchedule( 200 ) );
        task.setEnabled( false );
        
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
        
        Thread.sleep( 300 );
        
        assertNull( task.getLastRun() );
        
        assertNotNull( task.getNextRun() );
        
        assertEquals( 1, defaultScheduler.getAllTasks().size() );
    }
    
    private Schedule getTestSchedule( long waitTime )
    {
        Date startDate = new Date( System.currentTimeMillis() + waitTime );
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTime( startDate );
        tempCalendar.add( Calendar.DATE, 7 );
        Date endDate = tempCalendar.getTime();
        
        return  new DailySchedule(startDate, endDate);
    }
                                     

    private ScheduledTask<Integer> getTask()
    {
        Date startDate = new Date();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTime( startDate );
        tempCalendar.add( Calendar.DATE, 7 );
        Date endDate = tempCalendar.getTime();
        
        DefaultScheduledTask<Integer> task = new DefaultScheduledTask<Integer>(
            "TestTask",
            "Test Task",
            "Type",
            defaultScheduler,
            new TestIntegerCallable(),
            new DailySchedule(startDate, endDate) );

        return task;
    }

    public class TestIntegerCallable
        implements Callable<Integer>
    {
        private int runCount = 0;

        public Integer call()
            throws Exception
        {
            return ++runCount;
        }

        public int getRunCount()
        {
            return runCount;
        }
    }

}
