package org.sonatype.nexus.scheduling;

import java.util.concurrent.ExecutionException;

public interface SubmittedCallableTask<T>
    extends SubmittedTask
{
    T get()
        throws ExecutionException,
            InterruptedException;

    T getIfDone();
}
