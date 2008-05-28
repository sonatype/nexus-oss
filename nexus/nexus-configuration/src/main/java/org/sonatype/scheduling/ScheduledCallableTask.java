package org.sonatype.scheduling;

public interface ScheduledCallableTask<T>
    extends ScheduledTask, IteratingCallableTask<T>
{
}
