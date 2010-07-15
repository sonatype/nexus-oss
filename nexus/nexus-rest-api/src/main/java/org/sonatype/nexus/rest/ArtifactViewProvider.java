package org.sonatype.nexus.rest;

import java.io.IOException;

import org.restlet.data.Request;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * Provides an alternative view of an artifact / file.
 * 
 * @author Brian Demers
 */
public interface ArtifactViewProvider
{
    /**
     * Returns an object that represents a view for the storeRequest.
     * 
     * @param store The ResourceStore that was about to be "asked" for content.
     * @param storeRequest The store request to retrieve the view for.
     * @param item The item retrieved or null if not found. View provider must handle nulls.
     * @param req The REST request.
     * @return An object representing the view.
     * @throws IOException
     */
    public Object retrieveView( ResourceStore store, ResourceStoreRequest request, StorageItem item, Request req )
        throws IOException;
}
