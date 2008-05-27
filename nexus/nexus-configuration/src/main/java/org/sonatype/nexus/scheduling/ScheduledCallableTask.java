package org.sonatype.nexus.scheduling;

import java.util.concurrent.ExecutionException;

public interface ScheduledCallableTask<T>
    extends ScheduledTask, SubmittedCallableTask<T>
{
    T getLast()
        throws ExecutionException,
            InterruptedException;

    T getLastIfDone();
}
