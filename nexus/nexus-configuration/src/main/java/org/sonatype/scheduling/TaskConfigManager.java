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

/**
 * Manage the storage and loading of ScheduledTask objects
 */
public interface TaskConfigManager
{
    String ROLE = TaskConfigManager.class.getName();

    /**
     * Add a new scheduled task
     * 
     * @param <T>
     * @param task
     */
    public <T> void addTask( ScheduledTask<T> task );

    /**
     * Remove an existing scheduled task
     * 
     * @param <T>
     * @param task
     */
    public <T> void removeTask( ScheduledTask<T> task );

    /**
     * Create and start all tasks, usually done once upon starting system (to start tasks that should be recurring)
     * 
     * @param scheduler
     */
    public void initializeTasks( Scheduler scheduler );
}
