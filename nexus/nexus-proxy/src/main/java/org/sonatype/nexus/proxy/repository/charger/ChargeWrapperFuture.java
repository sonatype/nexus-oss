package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Preconditions;

/**
 * A Future wrapper that simply holds reference to ChargeWrapper, and all methods of Future are delegated to
 * ChargeWrapper's Future instance.
 * 
 * @author cstamas
 * @param <E>
 */
public class ChargeWrapperFuture<E>
    implements Future<E>
{
    private final ChargeWrapper<E> chargeWrapper;

    private final Future<E> future;

    public ChargeWrapperFuture( final ChargeWrapper<E> chargeWrapper, final Future<E> future )
    {
        this.chargeWrapper = Preconditions.checkNotNull( chargeWrapper );
        this.future = Preconditions.checkNotNull( future );
    }

    public ChargeWrapper<E> getChargeWrapper()
    {
        return chargeWrapper;
    }

    @Override
    public boolean cancel( boolean mayInterruptIfRunning )
    {
        return future.cancel( mayInterruptIfRunning );
    }

    @Override
    public boolean isCancelled()
    {
        return future.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return future.isDone();
    }

    @Override
    public E get()
        throws InterruptedException, ExecutionException
    {
        return future.get();
    }

    @Override
    public E get( long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return future.get( timeout, unit );
    }
}
