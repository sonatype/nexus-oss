package org.sonatype.security.usermanagement;

public class NoSuchUserManagerException
    extends Exception
{

    public NoSuchUserManagerException()
    {
        super();
    }

    public NoSuchUserManagerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchUserManagerException( String message )
    {
        super( message );
    }

    public NoSuchUserManagerException( Throwable cause )
    {
        super( cause );
    }

}
