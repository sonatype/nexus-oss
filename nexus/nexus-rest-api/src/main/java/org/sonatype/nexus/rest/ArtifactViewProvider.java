package org.sonatype.nexus.rest;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;

/**
 * Provides an alternative view of an artifact / file.
 * @author Brian Demers
 *
 */
public interface ArtifactViewProvider
{
    /**
     * Returns an object that represents a view for the storeRequest.
     * 
     * @param storeRequest The request to retrieve the view for.
     * @return An object representing the view.
     * @throws StorageException 
     */
    public Object retrieveView( ResourceStoreRequest storeRequest ) throws StorageException;
}
