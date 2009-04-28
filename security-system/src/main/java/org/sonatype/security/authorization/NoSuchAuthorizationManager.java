package org.sonatype.security.authorization;

public class NoSuchAuthorizationManager
    extends Exception
{

    public NoSuchAuthorizationManager()
    {
        super();
    }

    public NoSuchAuthorizationManager( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchAuthorizationManager( String message )
    {
        super( message );
    }

    public NoSuchAuthorizationManager( Throwable cause )
    {
        super( cause );
    }

}
