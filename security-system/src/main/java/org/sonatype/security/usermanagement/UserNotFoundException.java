package org.sonatype.security.usermanagement;

public class UserNotFoundException
    extends Exception
{

    public UserNotFoundException( String userId, String message, Throwable cause )
    {
        super( buildMessage( userId, message ), cause );
    }

    public UserNotFoundException( String userId )
    {
        super( buildMessage( userId, "" ) );
    }

    private static String buildMessage( String userId, String message )
    {
        return "User: '" + userId + "' could not be found. "+ message;
    }

}
