package org.sonatype.nexus.plugin.discovery;

public class NexusDiscoveryException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    public NexusDiscoveryException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public NexusDiscoveryException( final String message )
    {
        super( message );
    }

}
