/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.scheduling;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
     * Returns a name of the task.
     * 
     * @return
     */
    String getName();

    /**
     * Sets the name of the ScheduledTask.
     * 
     * @param name
     */
    void setName( String name );

    /**
     * Returns the "type" of the task.
     * 
     * @return
     */
    Class<?> getType();

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
     * Runs the task right now, putting schedule on hold until complete
     */
    void runNow();

    /**
     * Cancels the task and removes it from queue.
     */
    void cancel();

    /**
     * Resets the task state and reschedules if needed.
     */
    void reset();

    /**
     * Returns an exception is TaskState is BROKEN, null in any other case.
     * 
     * @return null, if task in not in BROKEN status, otherwise the exception that broke it.
     */
    Throwable getBrokenCause();

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
     * 
     * @return
     */
    Schedule getSchedule();

    /**
     * Sets the Schedule that is being used
     */
    void setSchedule( Schedule schedule );

    Map<String, String> getTaskParams();
}
