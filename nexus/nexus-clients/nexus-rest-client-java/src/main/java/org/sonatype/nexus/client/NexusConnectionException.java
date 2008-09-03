package org.sonatype.nexus.client;

/**
 * Thrown when a NexusClient cannot connect to a Nexus instance, or the Nexus instance returns a non success response.
 */
public class NexusConnectionException
    extends Exception
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -5163493126499979929L;

    public NexusConnectionException()
    {
        super();
    }

    public NexusConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NexusConnectionException( String message )
    {
        super( message );
    }

    public NexusConnectionException( Throwable cause )
    {
        super( cause );
    }

}
