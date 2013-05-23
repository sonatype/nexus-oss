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

import java.util.HashMap;
import java.util.Map;

public class SimpleTaskConfigManager
    implements TaskConfigManager
{
    private Map<String, ScheduledTask<?>> tasks;

    public SimpleTaskConfigManager()
    {
        super();

        tasks = new HashMap<String, ScheduledTask<?>>();
    }

    public void initializeTasks( Scheduler scheduler )
    {
        // nothing here, it is not persistent
    }

    public <T> void addTask( ScheduledTask<T> task )
    {
        tasks.put( task.getId(), task );
    }

    public <T> void removeTask( ScheduledTask<T> task )
    {
        tasks.remove( task.getId() );
    }

    public SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return null;
    }

    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        return null;
    }
}
