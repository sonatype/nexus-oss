package org.sonatype.scheduling;

import java.util.concurrent.ExecutionException;

public interface ScheduledCallableTask<T>
    extends ScheduledTask, SubmittedCallableTask<T>
{
    T getLast()
        throws ExecutionException,
            InterruptedException;

    T getLastIfDone();

    int getResultCount();

    T get( int i )
        throws IndexOutOfBoundsException;
}
