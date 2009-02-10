package org.sonatype.nexus.repository.metadata;

public class MetadataHandlerException
    extends Exception
{
    private static final long serialVersionUID = 1748381444529675486L;

    public MetadataHandlerException( String message )
    {
        super( message );
    }

    public MetadataHandlerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MetadataHandlerException( Throwable cause )
    {
        super( cause );
    }
}
