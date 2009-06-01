/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.storage.local;

import java.net.URL;
import java.util.Collection;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * Local storage.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface LocalRepositoryStorage
{
    /**
     * Returns a designator to identify the local storage implementation (hint: for example the Plexus role hint).
     * 
     * @return
     */
    String getProviderId();
    
    /**
     * Validate that the URL that defines storage location is valid.
     * 
     * @param url
     * @throws StorageException
     */
    void validateStorageUrl( String url )
        throws StorageException;

    /**
     * Check local storage for reachability.
     * 
     * @param uid the uid
     * @return true, if available (reachable)
     * @throws StorageException the storage exception
     */
    boolean isReachable( Repository repository, ResourceStoreRequest request )
        throws StorageException;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    URL getAbsoluteUrlFromBase( Repository repository, ResourceStoreRequest request )
        throws StorageException;

    /**
     * Gets the attributes handler.
     * 
     * @return the attributes handler
     */
    AttributesHandler getAttributesHandler();

    /**
     * Sets the attributes handler.
     * 
     * @param attributesHandler the new attributes handler
     */
    void setAttributesHandler( AttributesHandler attributesHandler );

    /**
     * Touch item and sets on it the current time.
     * 
     * @param uid the uid
     * @throws StorageException the storage exception
     */
    void touchItemRemoteChecked( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Touch item and sets on it the given timestamp.
     * 
     * @param uid the uid
     * @param timestamp the ts to set on file, 0 to "expire" it
     * @throws StorageException the storage exception
     */
    void touchItemRemoteChecked( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Touch item last requested and sets on it the current time.
     * 
     * @param uid the uid
     * @throws StorageException the storage exception
     */
    void touchItemLastRequested( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Touch item last requested and sets on it the given timestamp.
     * 
     * @param uid the uid
     * @param timestamp the ts to set on file, 0 to "expire" it
     * @throws StorageException the storage exception
     */
    void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Contains item.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( Repository repository, ResourceStoreRequest request )
        throws StorageException;

    /**
     * Retrieve item.
     * 
     * @param uid the uid
     * @return the abstract storage item
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    AbstractStorageItem retrieveItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Store item.
     * 
     * @param item the item
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void storeItem( Repository repository, StorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException;

    /**
     * Update item attributes, does not modify the content of it.
     * 
     * @param item the item
     * @throws StorageException the storage exception
     */
    void updateItemAttributes( Repository repository, ResourceStoreRequest request, StorageItem item )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Delete item, using wastebasket.
     * 
     * @param uid the uid
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void deleteItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException;

    /**
     * Shred item, avoid wastebasket.
     * 
     * @param uid the uid
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void shredItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException;

    /**
     * List items.
     * 
     * @param uid the uid
     * @return the collection< storage item>
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    Collection<StorageItem> listItems( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            StorageException;

}
