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
package org.sonatype.nexus.proxy.storage.remote;

import java.net.URL;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Remote storage.
 * 
 * @author cstamas
 */
public interface RemoteRepositoryStorage
{

    String ROLE = RemoteRepositoryStorage.class.getName();

    /**
     * Check remote storage for reachability.
     * 
     * @param uid the uid
     * @return true, if available (reachable)
     * @throws StorageException the storage exception
     */
    boolean isReachable( Repository repository )
        throws StorageException;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     * @throws StorageException when the repository in question has wrong/malformed URL set
     */
    URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
        throws StorageException;

    /**
     * Check remote storage if contains item.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( RepositoryItemUid uid )
        throws StorageException;

    /**
     * Check remote storage if contains item newer then newerThen.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( RepositoryItemUid uid, long newerThen )
        throws StorageException;

    /**
     * Retrieve item unconditionally.
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
     * Delete item.
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

}
