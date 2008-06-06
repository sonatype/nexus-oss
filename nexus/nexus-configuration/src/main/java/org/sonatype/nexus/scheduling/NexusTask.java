package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.concurrent.Callable;

import org.codehaus.plexus.logging.LogEnabled;
import org.sonatype.scheduling.ScheduledTask;

public interface NexusTask<T>
    extends Callable<T>, LogEnabled
{
    boolean allowConcurrentExecution( List<ScheduledTask<?>> existingTasks );
}
