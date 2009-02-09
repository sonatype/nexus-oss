package org.sonatype.nexus.repository.metadata;

public class MetadadaHandlerException
    extends Exception
{
    private static final long serialVersionUID = 1748381444529675486L;

    public MetadadaHandlerException( String message )
    {
        super( message );
    }

    public MetadadaHandlerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MetadadaHandlerException( Throwable cause )
    {
        super( cause );
    }
}
