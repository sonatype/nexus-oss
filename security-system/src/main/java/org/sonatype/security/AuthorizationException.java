package org.sonatype.security;

public class AuthorizationException
    extends Exception
{

    public AuthorizationException()
    {
    }

    public AuthorizationException( String message )
    {
        super( message );
    }

    public AuthorizationException( Throwable cause )
    {
        super( cause );
    }

    public AuthorizationException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
