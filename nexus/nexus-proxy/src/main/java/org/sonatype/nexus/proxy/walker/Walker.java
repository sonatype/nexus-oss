package org.sonatype.nexus.proxy.walker;

import java.util.List;

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
    void walk( WalkerContext context, List<WalkerProcessor> processors )
        throws WalkerException;

    /**
     * Walks from the path given on resource store, local only and not only collections.
     * 
     * @param fromPath
     * @throws WalkerException
     */
    void walk( WalkerContext context, String fromPath, List<WalkerProcessor> processors )
        throws WalkerException;

    /**
     * Walks from root with given paramters.
     * 
     * @param localOnly
     * @param collectionsOnly
     * @throws WalkerException
     */
    void walk( WalkerContext context, boolean localOnly, boolean collectionsOnly, List<WalkerProcessor> processors )
        throws WalkerException;

    /**
     * Walks from given path with given paramters.
     * 
     * @param fromPath
     * @param localOnly
     * @param collectionsOnly
     * @throws WalkerException
     */
    void walk( WalkerContext context, String fromPath, boolean localOnly, boolean collectionsOnly,
        List<WalkerProcessor> processors )
        throws WalkerException;
}
