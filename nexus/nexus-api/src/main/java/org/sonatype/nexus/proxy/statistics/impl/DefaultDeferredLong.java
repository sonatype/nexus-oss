package org.sonatype.nexus.proxy.statistics.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.sonatype.nexus.proxy.statistics.DeferredLong;

public class DefaultDeferredLong
    implements DeferredLong
{
    private final Future<Long> future;

    public DefaultDeferredLong( final Long fixedValue )
    {
        this(new FakeFuture<Long>( fixedValue ));
    }

    public DefaultDeferredLong( Future<Long> future )
    {
        this.future = future;
    }

    public boolean isDone()
    {
        return future.isDone();
    }

    public Long getValue()
    {
        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            return 0L;
        }
        catch ( ExecutionException e )
        {
            return 0L;
        }
    }

}
