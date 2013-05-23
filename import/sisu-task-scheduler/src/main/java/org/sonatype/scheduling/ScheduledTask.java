/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

public interface ScheduledTask<T>
{
    /**
     * Returns the task if it is an instance of SchedulerTask<?> or null if that's not the case (there is just a
     * Callable<?>).
     * 
     * @return
     */
    SchedulerTask<T> getSchedulerTask();

    /**
     * Returns the progress listener of this run, if the task runs, otherwise null.
     * 
     * @return
     */
    ProgressListener getProgressListener();

    /**
     * Returns the task (callable being run).
     * 
     * @return
     */
    Callable<T> getTask();

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
     * Runs the task right now, putting schedule on hold until complete
     */
    void runNow();

    /**
     * Cancels the task and does not removes it from queue (if it has schedule in future).
     */
    void cancelOnly();

    /**
     * Cancels the task and removes it from queue.
     */
    void cancel();

    /**
     * Cancels the task and removes it from queue (as {@link #cancel()} does), but if the passed in flag is {@code true}
     * it will interrupt the thread too.
     * 
     * @param interrupt
     */
    void cancel( boolean interrupt );

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
        throws ExecutionException, InterruptedException;

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
     * Returns the last run date of task, if any. Null otherwise.
     * 
     * @return
     */
    TaskState getLastStatus();

    /**
     * How much time last execution took in miliseconds
     * 
     * @return
     */
    Long getDuration();

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
