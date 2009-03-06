package org.sonatype.nexus.test.utils;

public class NexusIllegalStateException
    extends Exception
{

    private static final long serialVersionUID = -7898195673031677742L;

    public NexusIllegalStateException()
    {
        this( null );
    }

    public NexusIllegalStateException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NexusIllegalStateException( String message )
    {
        this( message, null );
    }

}
