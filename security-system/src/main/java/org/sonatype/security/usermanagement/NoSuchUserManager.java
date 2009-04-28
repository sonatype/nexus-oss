package org.sonatype.security.usermanagement;

public class NoSuchUserManager
    extends Exception
{

    public NoSuchUserManager()
    {
        super();
    }

    public NoSuchUserManager( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchUserManager( String message )
    {
        super( message );
    }

    public NoSuchUserManager( Throwable cause )
    {
        super( cause );
    }

}
