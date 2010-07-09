package org.sonatype.nexus.rest;

import java.io.IOException;

import org.restlet.data.Request;
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
     * @param storeRequest The request to retrieve the view for.
     * @param item
     * @param req
     * @return An object representing the view.
     * @throws IOException
     */
    public Object retrieveView( StorageItem item, Request req )
        throws IOException;
}
