package org.sonatype.security.authorization;

/**
 * Thrown when a Role could not be found.
 * 
 * @author Brian Demers
 */
public class NoSuchRoleException
    extends Exception
{
    private static final long serialVersionUID = -3551757972830003397L;

    public NoSuchRoleException()
    {
        super();
    }

    public NoSuchRoleException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchRoleException( String message )
    {
        super( message );
    }

    public NoSuchRoleException( Throwable cause )
    {
        super( cause );
    }

}
