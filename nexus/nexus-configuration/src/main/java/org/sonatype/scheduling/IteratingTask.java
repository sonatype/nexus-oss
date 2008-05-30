package org.sonatype.scheduling;

import java.util.Date;
import java.util.List;

import org.sonatype.scheduling.iterators.SchedulerIterator;

public interface IteratingTask<T>
    extends SubmittedTask<T>
{
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
     * Returns the SchedulerIterator, that generates dates for executions.
     * 
     * @return
     */
    SchedulerIterator getScheduleIterator();

    /**
     * Returns the list of accumulated results.
     * 
     * @return
     */
    List<T> getResults();
}
