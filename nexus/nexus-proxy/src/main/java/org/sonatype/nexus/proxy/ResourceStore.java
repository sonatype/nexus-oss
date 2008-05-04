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
package org.sonatype.nexus.proxy;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;

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
     * Retrieves item from the path of the request.
     * 
     * @param request the request
     * @return the storage item
     * @throws NoSuchResourceStoreException the no such store exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    StorageItem retrieveItem( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Copies the item from <code>from</code> to <code>to</code>. Retrieval may involve remote access unless
     * request forbids it, the storing involves local storage only.
     * 
     * @param from the from
     * @param to the to
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Moves the item from <code>from</code> to <code>to</code>. Retrieval may involve remote access unless request
     * forbids it, the storing involves local storage only.
     * 
     * @param from the from
     * @param to the to
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Deletes item from the request path. Involves local storage only.
     * 
     * @param request the request
     * @throws StorageException the storage exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws AccessDeniedException the access denied exception
     */
    void deleteItem( ResourceStoreRequest request )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores item onto request path, with content supplied by stream. Involves local storage only.
     * 
     * @param request the request
     * @param is the is
     * @param userAttributes the user attributes
     * @throws StorageException the storage exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;
    
    /**
     * Creates a collection (directory) on requested path. Involves local storage only.
     * 
     * @param request the request
     * @param userAttributes the user attributes
     * @throws StorageException the storage exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Lists the path denoted by item.
     * 
     * @param request the request
     * @return the collection< storage item>
     * @throws NoSuchResourceStoreException the no such repository exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

}
