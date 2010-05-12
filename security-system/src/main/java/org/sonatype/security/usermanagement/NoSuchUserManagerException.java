package org.sonatype.security.usermanagement;

/**
 * Thrown when UserManager could not be found.
 * 
 * @author Brian Demers
 */
public class NoSuchUserManagerException
    extends Exception
{

    private static final long serialVersionUID = -2561129270233203244L;

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
