package org.sonatype.security.authorization;

public class NoSuchPrivilegeException
    extends Exception
{

    public NoSuchPrivilegeException()
    {
        super();
    }

    public NoSuchPrivilegeException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchPrivilegeException( String message )
    {
        super( message );
    }

    public NoSuchPrivilegeException( Throwable cause )
    {
        super( cause );
    }

}
