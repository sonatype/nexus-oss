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
}
