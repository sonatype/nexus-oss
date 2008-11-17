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
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.NexusService;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.Schedule;

public interface NexusScheduler
    extends NexusService
{
    /**
     * Issue a NexusTask for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @return
     */
    <T> ScheduledTask<T> submit( String name, NexusTask<T> nexusTask )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a NexusTask for scheduled execution.
     * 
     * @param name
     * @param runnable
     * @param iterator
     * @return
     */
    <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Update parameters of a scheduled task
     * 
     * @param task
     * @return
     */
    <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
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

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    NexusTask<?> createTaskInstance( Class<?> taskType )
        throws IllegalArgumentException;
}
