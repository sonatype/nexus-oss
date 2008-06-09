package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.codehaus.plexus.logging.LogEnabled;
import org.sonatype.scheduling.ScheduledTask;

public interface NexusTask<T>
    extends Callable<T>, LogEnabled
{
    boolean allowConcurrentExecution( List<ScheduledTask<?>> existingTasks );
    
    void addParameter(String key, String value);
    
    String getParameter(String key);
    
    Map<String, String> getParameters();
}
