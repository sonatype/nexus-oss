/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.attributes;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface AttributesHandler. Used to decorate the items.
 * 
 * @author cstamas
 */
public interface AttributesHandler
{
    AttributeStorage getAttributeStorage();

    void setAttributeStorage( AttributeStorage attributeStorage );

    /**
     * Fetches the item attributes and decorates the supplied item.
     * 
     * @param item the item
     * @return Map of attributes or empty map if none found.
     */
    void fetchAttributes( StorageItem item );

    /**
     * Creates the item attributes and stores them.
     * 
     * @param item the item
     * @param inputStream the input stream
     */
    void storeAttributes( StorageItem item, ContentLocator content );

    /**
     * Removes the item attributes.
     * 
     * @param uid the uid
     * @return true if attributes are found and deleted, false otherwise.
     */
    boolean deleteAttributes( RepositoryItemUid uid );

    // ==

    /**
     * Touch item and sets on it the current time.
     * 
     * @param uid the uid
     * @throws LocalStorageException the storage exception
     */
    void touchItemRemoteChecked( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item and sets on it the given timestamp.
     * 
     * @param uid the uid
     * @param timestamp the ts to set on file, 0 to "expire" it
     * @throws LocalStorageException the storage exception
     */
    void touchItemRemoteChecked( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item last requested and sets on it the current time.
     * 
     * @param uid the uid
     * @throws LocalStorageException the storage exception
     */
    void touchItemLastRequested( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item last requested and sets on it the given timestamp.
     * 
     * @param uid the uid
     * @param timestamp the ts to set on file, 0 to "expire" it
     * @throws LocalStorageException the storage exception
     */
    void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch only if request is user-request (coming from outside of nexus).
     * 
     * @param timestamp
     * @param repository
     * @param request
     * @param storageItem
     * @throws ItemNotFoundException
     * @throws LocalStorageException
     */
    void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request,
                                 StorageItem storageItem )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Update item attributes, does not modify the content of it.
     * 
     * @param item the item
     * @throws LocalStorageException the storage exception
     */
    void updateItemAttributes( Repository repository, ResourceStoreRequest request, StorageItem item )
        throws ItemNotFoundException, LocalStorageException;
}
