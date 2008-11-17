package org.sonatype.nexus.scheduling;

/**
 * A TaskFilter is a simple way to decide whether a task may run or may not run against some domain (ie. a Repository).
 * 
 * @author cstamas
 */
public interface TaskFilter
{
    boolean allowsScheduledTasks();

    boolean allowsUserInitiatedTasks();
}
