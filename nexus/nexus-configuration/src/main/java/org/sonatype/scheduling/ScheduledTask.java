package org.sonatype.scheduling;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

public interface ScheduledTask<T>
{
    /**
     * Returns a unique ID of the task.
     * 
     * @return
     */
    String getId();

    /**
     * Returns the "type" of the task.
     * 
     * @return
     */
    String getType();

    /**
     * Returns the task state.
     * 
     * @return
     */
    TaskState getTaskState();

    /**
     * Returns the date when the task is scheduled.
     * 
     * @return
     */
    Date getScheduledAt();

    /**
     * Cancels the task and removes it from queue.
     */
    void cancel();

    /**
     * Returns an exception is TaskState is BROKEN, null in any other case.
     * 
     * @return null, if task in not in BROKEN status, otherwise the exception that broke it.
     */
    Exception getBrokenCause();

    /**
     * Gets the result of Callable, or null if it is "converted" from Runnable. It behaves just like Future.get(), if
     * the task is not finished, it will block.
     * 
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    T get()
        throws ExecutionException,
            InterruptedException;

    /**
     * Gets the result of Callable, or null if it is "converted" from Runnable.
     * 
     * @return
     */
    T getIfDone();
    
    /**
     * Returns the last run date of task, if any. Null otherwise.
     * 
     * @return
     */
    Date getLastRun();

    /**
     * Returns the next run date of task.
     * 
     * @return
     */
    Date getNextRun();

    /**
     * Is the task enabled? If the task is enabled, it is executing when it needs to execute. If the task is disabled,
     * it will still "consume" it's schedules, but will do nothing (NOP).
     * 
     * @return
     */
    boolean isEnabled();

    /**
     * Sets enabled.
     * 
     * @param enabled
     */
    void setEnabled( boolean enabled );

    /**
     * Returns the list of accumulated results.
     * 
     * @return
     */
    List<T> getResults();
    
    /**
     * Returns the iterator that is being used to repeat the task
     * 
     * @return
     */
    SchedulerIterator getScheduleIterator();
    
    /**
     * Returns the Schedule that is being used
     * @return
     */
    Schedule getSchedule();
}
