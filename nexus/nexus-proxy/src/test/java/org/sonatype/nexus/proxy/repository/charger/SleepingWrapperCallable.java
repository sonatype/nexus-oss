package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

public class SleepingWrapperCallable<E>
    implements Callable<E>
{
    private final long sleepTimeMillis;

    private final Callable<E> callable;

    public SleepingWrapperCallable( final long sleepTimeMillis, final Callable<E> callable )
    {
        this.sleepTimeMillis = sleepTimeMillis;

        this.callable = Preconditions.checkNotNull( callable );
    }

    @Override
    public E call()
        throws Exception
    {
        try
        {
            Thread.sleep( sleepTimeMillis );

            return callable.call();
        }
        catch ( InterruptedException e )
        {
            System.out.println( e.toString() );

            throw e;
        }
    }
}
