package org.sonatype.scheduling;

import java.util.concurrent.ExecutionException;

public interface SubmittedCallableTask<T>
    extends SubmittedTask
{
    T get()
        throws ExecutionException,
            InterruptedException;

    T getIfDone();
}
