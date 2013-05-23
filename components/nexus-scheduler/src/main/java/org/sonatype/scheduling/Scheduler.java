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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.schedules.Schedule;

public interface Scheduler
{
    /**
     * Loads up persisted tasks from TaskConfigManager and initializes all of them (to call on startup).
     */
    void initializeTasks();

    /**
     * Shuts down the scheduler cleanly.
     */
    void shutdown();

    /**
     * Initialize a task on bootup.
     * 
     * @param id
     * @param name
     * @param type
     * @param callable
     * @param schedule
     * @param enabled
     * @return
     * @throws RejectedExecutionException
     * @throws NullPointerException
     */
    <T> ScheduledTask<T> initialize( String id, String name, String type, Callable<T> callable, Schedule schedule,
                                     boolean enabled )
        throws RejectedExecutionException, NullPointerException;

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    ScheduledTask<Object> submit( String name, Runnable runnable )
        throws RejectedExecutionException, NullPointerException;

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
    ScheduledTask<Object> schedule( String name, Runnable runnable, Schedule schedule )
        throws RejectedExecutionException, NullPointerException;

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    <T> ScheduledTask<T> submit( String name, Callable<T> callable )
        throws RejectedExecutionException, NullPointerException;

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
    <T> ScheduledTask<T> schedule( String name, Callable<T> callable, Schedule schedule )
        throws RejectedExecutionException, NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param task
     * @return
     */
    <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException, NullPointerException;

    /**
     * Returns the map of currently active tasks. The resturned collection is an unmodifiable snapshot. It may differ
     * from current one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<ScheduledTask<?>>> getActiveTasks();

    /**
     * Returns the map of all tasks. The resturned collection is an unmodifiable snapshot. It may differ from current
     * one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<ScheduledTask<?>>> getAllTasks();

    /**
     * Returns an active task by it's ID.
     * 
     * @param id
     * @return
     */
    ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException;

    @Deprecated
    SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException;
}
