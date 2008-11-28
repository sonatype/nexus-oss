package org.sonatype.nexus.proxy.walker;

/**
 * This is the actual Walker that walks the ResourceStorage.
 * 
 * @author cstamas
 */
public interface Walker
{
    /**
     * Walks from the root of resource store, local only and not only collections.
     * 
     * @throws WalkerException
     */
    void walk( WalkerContext context );

    /**
     * Walks from the path given on resource store, local only and not only collections.
     * 
     * @param fromPath
     * @throws WalkerException
     */
    void walk( WalkerContext context, String fromPath );
}
