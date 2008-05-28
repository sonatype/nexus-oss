package org.sonatype.nexus.scheduling;

import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.scheduling.SubmittedTask;

public interface NexusTask
    extends Runnable
{
    void setLogger( Logger logger );

    boolean allowConcurrentExecution( List<SubmittedTask> existingTasks );
}
