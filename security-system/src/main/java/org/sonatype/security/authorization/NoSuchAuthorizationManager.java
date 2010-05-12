package org.sonatype.security.authorization;

/**
 * Thrown when an AuthorizationManager could not be found.
 * 
 * @author Brian Demers
 */
public class NoSuchAuthorizationManager
    extends Exception
{

    private static final long serialVersionUID = -9130834235862218360L;

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
