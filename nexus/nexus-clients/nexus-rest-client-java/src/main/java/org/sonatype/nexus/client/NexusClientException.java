package org.sonatype.nexus.client;

/**
 *  Thrown when unexpected problem occur on the client side. 
 */
public class NexusClientException
    extends Exception
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 989102224012468495L;

    public NexusClientException()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( String message, Throwable cause )
    {
        super( message, cause );
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( String message )
    {
        super( message );
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( Throwable cause )
    {
        super( cause );
        // TODO Auto-generated constructor stub
    }

    
    
}
