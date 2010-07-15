package org.sonatype.security.authorization;

/**
 * Thrown when an AuthorizationManager could not be found.
 * 
 * @author Brian Demers
 */
public class NoSuchAuthorizationManagerException
    extends Exception
{

    private static final long serialVersionUID = -9130834235862218360L;

    public NoSuchAuthorizationManagerException()
    {
        super();
    }

    public NoSuchAuthorizationManagerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchAuthorizationManagerException( String message )
    {
        super( message );
    }

    public NoSuchAuthorizationManagerException( Throwable cause )
    {
        super( cause );
    }

}
