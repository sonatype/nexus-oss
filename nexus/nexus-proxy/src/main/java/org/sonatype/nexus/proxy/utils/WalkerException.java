package org.sonatype.nexus.proxy.utils;

/**
 * Thrown by walker if something terrible happened.
 * 
 * @author cstamas
 */
public class WalkerException
    extends RuntimeException
{
    private static final long serialVersionUID = 3197048259219625491L;

    public WalkerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public WalkerException( String message )
    {
        super( message );
    }

    public WalkerException( Throwable cause )
    {
        super( cause );
    }
}
