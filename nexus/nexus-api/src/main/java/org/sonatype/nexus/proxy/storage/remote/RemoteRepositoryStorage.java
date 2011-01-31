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
package org.sonatype.nexus.proxy.storage.remote;

import java.net.URL;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.plugin.ExtensionPoint;

/**
 * Remote storage.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface RemoteRepositoryStorage
{
    /**
     * Returns a designator to identify the remote storage implementation (hint: for example the Plexus role hint).
     * 
     * @return
     */
    String getProviderId();

    /**
     * Returns a version to identify the version of remote storage implementation.
     * 
     * @return
     */
    String getVersion();

    /**
     * Check remote storage for reachability.
     * 
     * @param uid the uid
     * @return true, if available (reachable)
     * @throws RemoteStorageException the storage exception
     */
    boolean isReachable( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     * @throws RemoteStorageException when the repository in question has wrong/malformed URL set
     */
    URL getAbsoluteUrlFromBase( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteStorageException;

    /**
     * Validate that the URL that defines storage location is valid.
     * 
     * @param url
     * @throws RemoteStorageException
     */
    void validateStorageUrl( String url )
        throws RemoteStorageException;

    /**
     * Check remote storage if contains item.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws RemoteStorageException the storage exception
     */
    boolean containsItem( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException;

    /**
     * Check remote storage if contains item newer then newerThen.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws RemoteStorageException the storage exception
     */
    boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException;

    /**
     * Retrieve item unconditionally.
     * 
     * @return the abstract storage item
     * @throws ItemNotFoundException the item not found exception
     * @throws RemoteStorageException the storage exception
     */
    AbstractStorageItem retrieveItem( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        throws ItemNotFoundException, RemoteAccessException, RemoteStorageException;

    /**
     * Store item.
     * 
     * @param item the item
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws RemoteStorageException the storage exception
     */
    void storeItem( ProxyRepository repository, StorageItem item )
        throws UnsupportedStorageOperationException, RemoteAccessException, RemoteStorageException;

    /**
     * Delete item.
     * 
     * @param uid the uid
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws RemoteStorageException the storage exception
     */
    void deleteItem( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteAccessException,
        RemoteStorageException;
}
