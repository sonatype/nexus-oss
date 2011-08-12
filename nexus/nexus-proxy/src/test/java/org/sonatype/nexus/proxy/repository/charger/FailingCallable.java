package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

public class FailingCallable<E>
    implements Callable<E>
{
    private final Exception exception;

    public FailingCallable( final Exception exception )
    {
        this.exception = exception;
    }

    @Override
    public E call()
        throws Exception
    {
        throw exception;
    }
}
