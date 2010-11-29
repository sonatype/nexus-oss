package org.sonatype.nexus.proxy.statistics.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Just a "fake" future to be able to sneak in fixed values.
 * 
 * @author cstamas
 * @param <T>
 */
public class FakeFuture<T>
    implements Future<T>
{
    private final T value;

    public FakeFuture( final T value )
    {
        this.value = value;
    }

    public boolean cancel( boolean mayInterruptIfRunning )
    {
        return false;
    }

    public boolean isCancelled()
    {
        return false;
    }

    public boolean isDone()
    {
        return true;
    }

    public T get()
        throws InterruptedException, ExecutionException
    {
        return value;
    }

    public T get( long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return get();
    }
}
