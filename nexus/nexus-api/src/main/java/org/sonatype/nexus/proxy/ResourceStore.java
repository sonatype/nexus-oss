/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * The base abstraction of Proximity. This interface is implemented by Repositories and also by Routers.
 * 
 * @author cstamas
 */
public interface ResourceStore
{

    /**
     * Returns the ID of the resourceStore.
     * 
     * @return the id
     */
    String getId();

    /**
     * If is user managed, the nexus core and nexus core UI handles the store. Thus, for reposes, users are allowed to
     * edit/drop the repository.
     * 
     * @return
     */
    boolean isUserManaged();

    /**
     * Sets is the store user managed.
     * 
     * @param val
     */
    void setUserManaged( boolean val );

    /**
     * Tells whether the resource store is exposed as Nexus content or not.
     * 
     * @return
     */
    boolean isExposed();

    /**
     * Sets the exposed flag.
     * 
     * @param val
     */
    void setExposed( boolean val );

    /**
     * Retrieves item from the path of the request.
     * 
     * @param request the request
     * @return the storage item
     * @throws NoSuchResourceStoreException the no such store exception
     * @throws IllegalOperationException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    StorageItem retrieveItem( ResourceStoreRequest request )
        throws ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Copies the item from <code>from</code> to <code>to</code>. Retrieval may involve remote access unless request
     * forbids it, the storing involves local storage only.
     * 
     * @param from the from
     * @param to the to
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws IllegalOperationException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Moves the item from <code>from</code> to <code>to</code>. Retrieval may involve remote access unless request
     * forbids it, the storing involves local storage only.
     * 
     * @param from the from
     * @param to the to
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws IllegalOperationException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Deletes item from the request path. Involves local storage only.
     * 
     * @param request the request
     * @throws UnsupportedStorageOperationException
     * @throws StorageException the storage exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws IllegalOperationException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws AccessDeniedException the access denied exception
     */
    void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores item onto request path, with content supplied by stream. Involves local storage only.
     * 
     * @param request the request
     * @param is the is
     * @param userAttributes the user attributes
     * @throws UnsupportedStorageOperationException
     * @throws StorageException the storage exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Creates a collection (directory) on requested path. Involves local storage only.
     * 
     * @param request the request
     * @param userAttributes the user attributes
     * @throws UnsupportedStorageOperationException
     * @throws StorageException the storage exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws IllegalOperationException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Lists the path denoted by item.
     * 
     * @param request the request
     * @return the collection< storage item>
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws IllegalOperationException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    Collection<StorageItem> list( ResourceStoreRequest request )
        throws ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    /**
     * Returns the target set belonging to ResourceStoreRequest.
     * 
     * @param request
     * @return
     */
    TargetSet getTargetsForRequest( ResourceStoreRequest request );
}
