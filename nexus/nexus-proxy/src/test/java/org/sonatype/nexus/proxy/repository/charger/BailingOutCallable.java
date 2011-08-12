package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

public class BailingOutCallable<E>
    implements Callable<E>
{
    private final boolean withException;

    public BailingOutCallable( final boolean withException )
    {
        this.withException = withException;
    }

    @Override
    public E call()
        throws Exception
    {
        if ( withException )
        {
            throw new InterruptedException( "bailing out" );
        }
        else
        {
            return null;
        }
    }
}
