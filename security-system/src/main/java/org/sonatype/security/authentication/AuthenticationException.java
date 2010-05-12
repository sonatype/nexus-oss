package org.sonatype.security.authentication;

/**
 * Thrown when a Subject or Principal could not be authenticated.
 * @author Brian Demers
 *
 */
public class AuthenticationException
    extends Exception
{

    private static final long serialVersionUID = 5307046352518675119L;

    public AuthenticationException()
    {
    }

    public AuthenticationException( String message )
    {
        super( message );
    }

    public AuthenticationException( Throwable cause )
    {
        super( cause );
    }

    public AuthenticationException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
