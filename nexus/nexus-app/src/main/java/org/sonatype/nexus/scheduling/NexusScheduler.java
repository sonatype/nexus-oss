package org.sonatype.nexus.scheduling;

import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.ScheduleIterator;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SubmittedTask;

public interface NexusScheduler
{
    String ROLE = NexusScheduler.class.getName();

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param runnable
     * @return
     */
    SubmittedTask submit( NexusTask nexusTask )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param runnable
     * @param iterator
     * @return
     */
    ScheduledTask schedule( NexusTask nexusTask, ScheduleIterator iterator )
        throws RejectedExecutionException,
            NullPointerException;

}
