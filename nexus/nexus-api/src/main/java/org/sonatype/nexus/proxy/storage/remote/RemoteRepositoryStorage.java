/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.net.URL;
import java.util.Map;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Remote storage.
 * 
 * @author cstamas
 */
public interface RemoteRepositoryStorage
{
    /**
     * Returns a designator to identify the remote storage implementation (hint: for example the Plexus role hint).
     * 
     * @return
     */
    String getName();

    /**
     * Check remote storage for reachability.
     * 
     * @param uid the uid
     * @return true, if available (reachable)
     * @throws StorageException the storage exception
     */
    boolean isReachable( ProxyRepository repository, Map<String, Object> context )
        throws RemoteAccessException,
            StorageException;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     * @throws StorageException when the repository in question has wrong/malformed URL set
     */
    URL getAbsoluteUrlFromBase( ProxyRepository repository, Map<String, Object> context, String path )
        throws StorageException;

    /**
     * Validate that the URL that defines storage location is valid.
     * 
     * @param url
     * @throws StorageException
     */
    void validateStorageUrl( String url )
        throws StorageException;

    /**
     * Check remote storage if contains item.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( ProxyRepository repository, Map<String, Object> context, String path )
        throws RemoteAccessException,
            StorageException;

    /**
     * Check remote storage if contains item newer then newerThen.
     * 
     * @param uid the uid
     * @return true, if successful
     * @throws StorageException the storage exception
     */
    boolean containsItem( long newerThen, ProxyRepository repository, Map<String, Object> context, String path )
        throws RemoteAccessException,
            StorageException;

    /**
     * Retrieve item unconditionally.
     * 
     * @param uid the uid
     * @return the abstract storage item
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    AbstractStorageItem retrieveItem( ProxyRepository repository, Map<String, Object> context, String path )
        throws ItemNotFoundException,
            RemoteAccessException,
            StorageException;

    /**
     * Store item.
     * 
     * @param item the item
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void storeItem( ProxyRepository repository, Map<String, Object> context, StorageItem item )
        throws UnsupportedStorageOperationException,
            RemoteAccessException,
            StorageException;

    /**
     * Delete item.
     * 
     * @param uid the uid
     * @throws ItemNotFoundException the item not found exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws StorageException the storage exception
     */
    void deleteItem( ProxyRepository repository, Map<String, Object> context, String path )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            RemoteAccessException,
            StorageException;
}
