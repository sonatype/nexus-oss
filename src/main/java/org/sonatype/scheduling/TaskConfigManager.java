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

/**
 * Manage the storage and loading of ScheduledTask objects
 */
public interface TaskConfigManager
{
    /**
     * Add a new scheduled task
     * 
     * @param <T>
     * @param task
     */
    <T> void addTask( ScheduledTask<T> task );

    /**
     * Remove an existing scheduled task
     * 
     * @param <T>
     * @param task
     */
    <T> void removeTask( ScheduledTask<T> task );

    /**
     * Create and start all tasks, usually done once upon starting system (to start tasks that should be recurring)
     * 
     * @param scheduler
     */
    void initializeTasks( Scheduler scheduler );

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
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
