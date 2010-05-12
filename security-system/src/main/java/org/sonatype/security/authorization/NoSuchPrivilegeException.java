package org.sonatype.security.authorization;

/**
 * Thrown when a Privilege could not be found.
 * 
 * @author Brian Demers
 */
public class NoSuchPrivilegeException
    extends Exception
{
    private static final long serialVersionUID = 820651866330926246L;

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
