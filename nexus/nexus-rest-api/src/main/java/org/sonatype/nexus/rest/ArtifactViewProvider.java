package org.sonatype.nexus.rest;

import org.sonatype.nexus.proxy.ResourceStoreRequest;

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
     */
    public Object retrieveView( ResourceStoreRequest storeRequest );
}
