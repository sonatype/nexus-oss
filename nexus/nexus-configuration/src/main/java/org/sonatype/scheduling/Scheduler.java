package org.sonatype.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

public interface Scheduler
{
    String ROLE = Scheduler.class.getName();

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    SubmittedTask<Object> submit( Runnable runnable )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    IteratingTask<Object> iterate( Runnable runnable, SchedulerIterator iterator )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    ScheduledTask<Object> schedule( Runnable runnable, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    <T> SubmittedTask<T> submit( Callable<T> callable )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    <T> IteratingTask<T> iterate( Callable<T> callable, SchedulerIterator iterator )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    <T> ScheduledTask<T> schedule( Callable<T> callable, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Returns the map of currently active tasks. The resturned collection is an unmodifiable snapshot. It may differ
     * from current one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<SubmittedTask<?>>> getActiveTasks();

    /**
     * Returns an active task by it's ID.
     * 
     * @param id
     * @return
     */
    SubmittedTask<?> getTaskById( String id )
        throws NoSuchTaskException;
}
