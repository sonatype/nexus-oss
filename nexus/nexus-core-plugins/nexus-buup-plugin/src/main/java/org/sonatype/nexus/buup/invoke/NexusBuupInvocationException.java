package org.sonatype.nexus.buup.invoke;

public class NexusBuupInvocationException
    extends Exception
{
    private static final long serialVersionUID = 7016969726199497569L;

    public NexusBuupInvocationException( String message )
    {
        super( message );
    }

    public NexusBuupInvocationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
