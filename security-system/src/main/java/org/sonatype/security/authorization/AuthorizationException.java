package org.sonatype.security.authorization;

/**
 * Thrown when authorization fails.
 * @author Brian Demers
 *
 */
public class AuthorizationException
    extends Exception
{

    private static final long serialVersionUID = -2410686526069723485L;

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
