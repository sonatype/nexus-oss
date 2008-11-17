package org.sonatype.nexus.scheduling;

import org.sonatype.scheduling.SchedulerTask;

/**
 * The base interface for all Tasks used in Nexus.
 * 
 * @author cstamas
 * @param <T>
 */
public interface NexusTask<T>
    extends SchedulerTask<T>
{
    TaskActivityDescriptor getTaskActivityDescriptor();
}
