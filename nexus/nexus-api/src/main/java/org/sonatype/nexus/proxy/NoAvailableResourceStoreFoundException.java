package org.sonatype.nexus.proxy;

/**
 * Thrown when a router cannot find any subsequent ResourceStore (Router, Repository) to pass over the request
 * processing.
 * 
 * @author cstamas
 */
public class NoAvailableResourceStoreFoundException
    extends NoSuchResourceStoreException
{
    private static final long serialVersionUID = 4194248788880331878L;

    public NoAvailableResourceStoreFoundException( String msg )
    {
        super( msg );
    }

    public NoAvailableResourceStoreFoundException( String msg, Throwable t )
    {
        super( msg, t );
    }
}
