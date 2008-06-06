package org.sonatype.scheduling;

import java.util.HashMap;
import java.util.List;

public interface TaskConfigManager
{
    String ROLE = TaskConfigManager.class.getName();
    
    public <T> void addTask( ScheduledTask<T> task );
    public <T> void removeTask( ScheduledTask<T> task );
    public HashMap<String, List<ScheduledTask<?>>> getTasks();
}
