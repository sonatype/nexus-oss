package org.sonatype.nexus.scheduling;

/**
 * A generic activity descriptor.
 * 
 * @author cstamas
 */
public interface TaskActivityDescriptor
{
    /**
     * Returns true if this is a scheduled task.
     * 
     * @return
     */
    boolean isScheduled();

    /**
     * Returns true if this is task that is initiated by user.
     * 
     * @return
     */
    boolean isUserInitiated();

    /**
     * Returns true if task having this activity desciptor is allowed to run.
     * 
     * @param filter
     * @return
     */
    boolean allowedExecution( TaskFilter filter );
}
