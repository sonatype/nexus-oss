package org.sonatype.scheduling;

import java.util.concurrent.Callable;

import org.codehaus.plexus.PlexusTestCase;

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
        
        task.cancel();
        
        callable.blockForDone();
        
        // Now check and see if task is still running...
        assertTrue( callable.isAllDone() );
    }
    
    public class RunForeverCallable
        implements Callable<Integer>
    {    
        private boolean allDone = false;
        
        private boolean started = false;
        
        public Integer call()
            throws Exception
        {
            try
            {
                while ( true )
                {
                    Thread.sleep( 1 );
                    started = true;
                }
            }
            finally
            {
                allDone = true;
            }
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
