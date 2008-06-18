/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.storage.local;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Local storage.
 * 
 * @author cstamas
 */
public interface LocalRepositoryStorage
{

    String ROLE = LocalRepositoryStorage.class.getName();

    /**
     * Check local storage for reachability.
     * 
     * @param uid the uid
     * @return true, if available (reachable)
     * @throws StorageException the storage exception
     */
    boolean isReachable( RepositoryItemUid uid )
        throws StorageException;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
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
    void touchItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Touch item and sets on it the given timestamp.
     * 
     * @param uid the uid
     * @param timestamp the ts to set on file, 0 to "expire" it
     * @throws StorageException the storage exception
     */
    void touchItem( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Contains item.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( RepositoryItemUid uid )
        throws StorageException;

    /**
     * Retrieve item.
     * 
     * @param uid the uid
     * @return the abstract storage item
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    AbstractStorageItem retrieveItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Retrieve item content.
     * 
     * @param uid the uid
     * @return the input stream
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    InputStream retrieveItemContent( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException;

    /**
     * Store item.
     * 
     * @param item the item
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException;

    /**
     * Delete item, using wastebasket.
     * 
     * @param uid the uid
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void deleteItem( RepositoryItemUid uid )
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
    void shredItem( RepositoryItemUid uid )
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
    Collection<StorageItem> listItems( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException;

}
