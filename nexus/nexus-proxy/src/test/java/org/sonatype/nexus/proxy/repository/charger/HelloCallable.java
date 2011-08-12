package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

public class HelloCallable
    implements Callable<String>
{
    private final String name;

    public HelloCallable( final String name )
    {
        this.name = name;
    }

    @Override
    public String call()
        throws Exception
    {
        return "hello " + name;
    }
}
