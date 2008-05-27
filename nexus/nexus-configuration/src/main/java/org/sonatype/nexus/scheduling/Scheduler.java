package org.sonatype.nexus.scheduling;

import java.util.concurrent.Callable;

public interface Scheduler
{
    String ROLE = Scheduler.class.getName();

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    SubmittedTask submit( Runnable runnable );

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    ScheduledTask schedule( Runnable runnable, ScheduleIterator iterator );

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    <T> SubmittedCallableTask<T> submit( Callable<T> callable );

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    <T> ScheduledCallableTask<T> schedule( Callable<T> callable, ScheduleIterator iterator );
}
