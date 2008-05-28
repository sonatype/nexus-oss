package org.sonatype.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.iterators.ScheduleIterator;
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
    SubmittedTask submit( Runnable runnable )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    IteratingTask iterate( Runnable runnable, ScheduleIterator iterator )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    ScheduledTask schedule( Runnable runnable, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    <T> SubmittedCallableTask<T> submit( Callable<T> callable )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    <T> IteratingCallableTask<T> iterate( Callable<T> callable, ScheduleIterator iterator )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    <T> ScheduledCallableTask<T> schedule( Callable<T> callable, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Returns the map of currently active tasks. The resturned collection is an unmodifiable snapshot. It may differ
     * from current one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<Class<?>, List<SubmittedTask>> getScheduledTasks();
}
