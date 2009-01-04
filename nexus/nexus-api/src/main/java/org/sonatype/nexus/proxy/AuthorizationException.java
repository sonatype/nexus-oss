package org.sonatype.nexus.proxy;

/**
 * Top level abstract class that is superclass for all authorization related exceptions.
 * 
 * @author cstamas
 */
public abstract class AuthorizationException
    extends Exception
{
    private static final long serialVersionUID = 391662938886542734L;

    public AuthorizationException( String message )
    {
        super( message );
    }

    public AuthorizationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public AuthorizationException( Throwable cause )
    {
        super( cause );
    }
}
