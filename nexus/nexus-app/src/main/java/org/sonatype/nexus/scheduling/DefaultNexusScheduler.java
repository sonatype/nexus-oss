package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.schedules.Schedule;

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

    public <T> ScheduledTask<T> submit( String name, NexusTask<T> nexusTask )
        throws RejectedExecutionException,
            NullPointerException
    {
        Class<?> cls = nexusTask.getClass();

        List<ScheduledTask<?>> existingTasks = scheduler.getActiveTasks().get( cls );

        if ( existingTasks == null || nexusTask.allowConcurrentExecution( existingTasks ) )
        {
            return scheduler.submit( name, nexusTask );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already submitted!" );
        }
    }

    public <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException
    {
        Class<?> cls = nexusTask.getClass();

        List<ScheduledTask<?>> existingTasks = scheduler.getActiveTasks().get( cls );

        if ( existingTasks == null || nexusTask.allowConcurrentExecution( existingTasks ) )
        {
            return scheduler.schedule( name, nexusTask, schedule );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already scheduled!" );
        }
    }

    public Map<String, List<ScheduledTask<?>>> getActiveTasks()
    {
        return scheduler.getActiveTasks();
    }

    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        return scheduler.getTaskById( id );
    }

}
