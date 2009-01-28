package org.sonatype.nexus.repository.metadata.validation;

public class ValidationException
    extends Exception
{
    private static final long serialVersionUID = -8892632174114363043L;

    public ValidationException( String message )
    {
        super( message );
    }

    public ValidationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ValidationException( Throwable cause )
    {
        super( cause );
    }
}
