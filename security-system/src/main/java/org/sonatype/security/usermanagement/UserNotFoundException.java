package org.sonatype.security.usermanagement;

/**
 * Thrown when a user could not be found.
 * 
 * @author Brian Demers
 */
public class UserNotFoundException
    extends Exception
{
    private static final long serialVersionUID = -177760017345640029L;

    public UserNotFoundException( String userId, String message, Throwable cause )
    {
        super( buildMessage( userId, message ), cause );
    }

    public UserNotFoundException( String userId, String message )
    {
        super( buildMessage( userId, message ) );
    }

    public UserNotFoundException( String userId )
    {
        super( buildMessage( userId, "" ) );
    }

    private static String buildMessage( String userId, String message )
    {
        return "User: '" + userId + "' could not be found. " + message;
    }

}
