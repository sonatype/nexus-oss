package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.logging.LoggerManager;
import org.sonatype.scheduling.ScheduleIterator;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.SubmittedTask;

/**
 * The Nexus scheduler.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexusScheduler
    implements NexusScheduler
{
    /**
     * The scheduler.
     * 
     * @plexus.requirement
     */
    private Scheduler scheduler;

    /**
     * The lm
     * 
     * @plexus.requirement
     */
    private LoggerManager loggerManager;

    public SubmittedTask submit( NexusTask nexusTask )
        throws RejectedExecutionException,
            NullPointerException
    {
        nexusTask.setLogger( loggerManager.getLoggerForComponent( nexusTask.getClass().getName() ) );

        Class<?> cls = nexusTask.getClass();

        List<SubmittedTask> existingTasks = scheduler.getScheduledTasks().get( cls );

        if ( existingTasks == null || nexusTask.allowConcurrentExecution( existingTasks ) )
        {
            return scheduler.submit( nexusTask );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already submitted!" );
        }
    }

    public ScheduledTask schedule( NexusTask nexusTask, ScheduleIterator iterator )
        throws RejectedExecutionException,
            NullPointerException
    {
        nexusTask.setLogger( loggerManager.getLoggerForComponent( nexusTask.getClass().getName() ) );

        Class<?> cls = nexusTask.getClass();

        List<SubmittedTask> existingTasks = scheduler.getScheduledTasks().get( cls );

        if ( existingTasks == null || nexusTask.allowConcurrentExecution( existingTasks ) )
        {
            return scheduler.schedule( nexusTask, iterator );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already scheduled!" );
        }
    }

}
