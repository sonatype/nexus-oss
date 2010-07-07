package org.sonatype.nexus.rest;

import java.io.IOException;

import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;

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
     * @param item
     * @return An object representing the view.
     * @throws ResourceException
     * @throws StorageException
     * @throws ItemNotFoundException
     * @throws IllegalOperationException
     * @throws NoSuchResourceStoreException
     * @throws AccessDeniedException
     * @throws IOException
     */
    public Object retrieveView( ResourceStoreRequest storeRequest, StorageItem item )
        throws ResourceException, IOException, AccessDeniedException, NoSuchResourceStoreException,
        IllegalOperationException, ItemNotFoundException, StorageException;
}
