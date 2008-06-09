package org.sonatype.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.schedules.Schedule;

public interface Scheduler
{
    String ROLE = Scheduler.class.getName();

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    ScheduledTask<Object> submit( String name, Runnable runnable, Map<String,String>taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param name
     * @param runnable
     * @param schedule
     * @param taskParams
     * @param store
     * @return
     */
    ScheduledTask<Object> schedule( String name, Runnable runnable, Schedule schedule, Map<String,String>taskParams, boolean store )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    <T> ScheduledTask<T> submit( String name, Callable<T> callable, Map<String,String>taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param name
     * @param runnable
     * @param schedule
     * @param taskParams
     * @param store
     * @return
     */
    <T> ScheduledTask<T> schedule( String name, Callable<T> callable, Schedule schedule, Map<String,String>taskParams, boolean store )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Returns the map of currently active tasks. The resturned collection is an unmodifiable snapshot. It may differ
     * from current one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<ScheduledTask<?>>> getActiveTasks();

    /**
     * Returns an active task by it's ID.
     * 
     * @param id
     * @return
     */
    ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException;
}
