package org.sonatype.nexus.proxy;

public class ResourceStoreIteratorRequest
    extends ResourceStoreRequest
{
    public enum Traversal
    {
        IN_DEPTH, IN_WIDTH;
    }

    private final Traversal traversal;

    public ResourceStoreIteratorRequest( Traversal traversal, String requestPath )
    {
        this( traversal, requestPath, true, false );
    }

    public ResourceStoreIteratorRequest( Traversal traversal, String requestPath, boolean localOnly, boolean remoteOnly )
    {
        super( requestPath, localOnly, remoteOnly );

        this.traversal = traversal;
    }

    public Traversal getTraversal()
    {
        return traversal;
    }
}
