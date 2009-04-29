package org.sonatype.security.authorization;

public class NoSuchRoleException
    extends Exception
{

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
