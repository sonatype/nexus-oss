package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

/**
 * A utility class to be able to supply wrapped Callable together with ExceptionHandler, if you did not implement those
 * as one class.
 * 
 * @author cstamas
 * @param <E>
 */
public class ExceptionHandlingCallable<E>
    implements Callable<E>, ExceptionHandler
{
    private final Callable<E> callable;

    private final ExceptionHandler exceptionHandler;

    public ExceptionHandlingCallable( final Callable<E> callable, final ExceptionHandler exceptionHandler )
    {
        this.callable = Preconditions.checkNotNull( callable );
        this.exceptionHandler = Preconditions.checkNotNull( exceptionHandler );
    }

    @Override
    public E call()
        throws Exception
    {
        return callable.call();
    }

    @Override
    public boolean handle( Exception ex )
    {
        return exceptionHandler.handle( ex );
    }
}
