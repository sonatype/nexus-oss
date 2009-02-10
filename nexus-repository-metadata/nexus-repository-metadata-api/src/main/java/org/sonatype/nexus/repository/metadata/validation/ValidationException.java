package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.MetadataHandlerException;

public class ValidationException
    extends MetadataHandlerException
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
