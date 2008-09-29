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
package org.sonatype.nexus.proxy.repository;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.EventMulticaster;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * Repository interface used by Proximity. It is an extension of ResourceStore iface, allowing to make direct
 * RepositoryItemUid based requests which bypasses AccessManager. Also, defines some properties.
 * 
 * @author cstamas
 */
public interface Repository
    extends EventMulticaster, ResourceStore, ConfigurationChangeListener
{
    String ROLE = Repository.class.getName();

    /**
     * This is the "class" of the repository content. It is used in grouping, only same content reposes may be grouped.
     * 
     * @return
     */
    ContentClass getRepositoryContentClass();

    /**
     * Returns the type of repository.
     * 
     * @return
     */
    RepositoryType getRepositoryType();

    /**
     * Sets the ID of the resourceStore. It must be unique type-wide (Router vs Repository).
     * 
     * @param id the ID of the repo.
     */
    void setId( String id );

    /**
     * Gets repository human name.
     * 
     * @return
     */
    String getName();

    /**
     * Sets repository human name.
     * 
     * @param name
     */
    void setName( String name );

    /**
     * Gets the not found cache time to live (in minutes).
     * 
     * @return the not found cache time to live (in minutes)
     */
    int getNotFoundCacheTimeToLive();

    /**
     * Sets the not found cache time to live (in minutes).
     * 
     * @param notFoundCacheTimeToLiveSeconds the new not found cache time to live (in minutes).
     */
    void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive );

    /**
     * Gets the not found cache.
     * 
     * @return the not found cache
     */
    PathCache getNotFoundCache();

    /**
     * Sets the not found cache.
     * 
     * @param notFoundcache the new not found cache
     */
    void setNotFoundCache( PathCache notFoundcache );

    /**
     * Adds path to NFC.
     * 
     * @param path
     */
    void addToNotFoundCache( String path );

    /**
     * Removes path from NFC.
     * 
     * @param path
     */
    void removeFromNotFoundCache( String path );

    /**
     * Gets the item max age in (in minutes).
     * 
     * @return the item max age in (in minutes)
     */
    int getItemMaxAge();

    /**
     * Sets the item max age in (in minutes).
     * 
     * @param itemMaxAgeInSeconds the new item max age in (in minutes).
     */
    void setItemMaxAge( int itemMaxAge );

    /**
     * Gets local status.
     */
    LocalStatus getLocalStatus();

    /**
     * Sets local status.
     * 
     * @param val the val
     */
    void setLocalStatus( LocalStatus val );

    /**
     * Gets remote status.
     */
    RemoteStatus getRemoteStatus( boolean forceCheck );

    /**
     * Gets proxy mode.
     * 
     * @return
     */
    ProxyMode getProxyMode();

    /**
     * Sets proxy mode.
     * 
     * @param val
     */
    void setProxyMode( ProxyMode val );

    /**
     * Gets the RepositoryStatusCheckMode.
     * 
     * @return
     */
    RepositoryStatusCheckMode getRepositoryStatusCheckMode();

    /**
     * Sets the RepositoryStatusCheckMode.
     * 
     * @param mode
     */
    void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode );

    /**
     * Is Repository listable?.
     * 
     * @return true if is listable, otherwise false.
     */
    boolean isBrowseable();

    /**
     * Sets the listable property of repository. If true, its content will be returned by listItems method, otherwise
     * not. The retrieveItem will still function and return the requested item.
     * 
     * @param val the val
     */
    void setBrowseable( boolean val );

    /**
     * Is Repository read-only?.
     * 
     * @return true if is read only, otherwise false.
     */
    boolean isAllowWrite();

    /**
     * Sets the read-only property of repository. If true, the repository will refuse to process any WRITE operation
     * coming from "outside". This does not affect the in-repository caching using LocalStorage. It just says, that from
     * the "outer" perspective, this repo behaves like read-only repository, and deployment is disabled for example.
     * 
     * @param val the val
     */
    void setAllowWrite( boolean val );

    /**
     * Is Repository indexable?.
     * 
     * @return true if is indexable, otherwise false.
     */
    boolean isIndexable();

    /**
     * Sets the indexable property of repository. If true, its content will be indexed by Indexer, otherwise not.
     * 
     * @param val the val
     */
    void setIndexable( boolean val );

    /**
     * Returns the local URL of this repository, if any.
     * 
     * @return local url of this repository, null otherwise.
     */
    String getLocalUrl();

    /**
     * Sets the local url.
     * 
     * @param url the new local url
     */
    void setLocalUrl( String url );

    /**
     * Returns the remote URL of this repository, if any.
     * 
     * @return remote url of this repository, null otherwise.
     */
    String getRemoteUrl();

    /**
     * Sets the remote url.
     * 
     * @param url the new remote url
     */
    void setRemoteUrl( String url );

    /**
     * Returns repository specific remote connection context.
     * 
     * @return null if none
     */
    RemoteStorageContext getRemoteStorageContext();

    /**
     * Sets the repository specific remote connection context.
     * 
     * @param ctx
     */
    void setRemoteStorageContext( RemoteStorageContext ctx );

    /**
     * Purges the caches (NFC and expires files) from path and below.
     * 
     * @param path a path from to start descending. If null, it is taken as "root".
     */
    void clearCaches( String path );

    /**
     * Evicts items that were last used before timestamp.
     * 
     * @param timestamp
     */
    Collection<String> evictUnusedItems( long timestamp );

    /**
     * Forces the recreation of attributes on this repository.
     * 
     * @param initialData the initial data
     * @return true, if recreate attributes
     */
    boolean recreateAttributes( String fromPath, Map<String, String> initialData );

    /**
     * Returns the repository level AccessManager. Per repository instance may exists.
     * 
     * @return the access manager
     */
    AccessManager getAccessManager();

    /**
     * Sets the repository level AccessManager. Per repository instance may exists.
     * 
     * @param accessManager the access manager
     */
    void setAccessManager( AccessManager accessManager );

    /**
     * Returns the local storage of the repository. Per repository instance may exists.
     * 
     * @return localStorage or null.
     */
    LocalRepositoryStorage getLocalStorage();

    /**
     * Sets the local storage of the repository. May be null if this is an aggregating repos without caching function.
     * Per repository instance may exists.
     * 
     * @param storage the storage
     */
    void setLocalStorage( LocalRepositoryStorage storage );

    /**
     * Returns the remoteStorage of the repository. Per repository instance may exists.
     * 
     * @return remoteStorage or null.
     */
    RemoteRepositoryStorage getRemoteStorage();

    /**
     * Sets the remote storage of the repository. May be null if this is a Local repository only. Per repository
     * instance may exists.
     * 
     * @param storage the storage
     */
    void setRemoteStorage( RemoteRepositoryStorage storage );

    /**
     * Retrieves item content from the path.
     * 
     * @param uid the uid
     * @return the input stream
     * @throws UnsupportedOperationException the unsupported operation exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    InputStream retrieveItemContent( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Retrieves item with content from the path.
     * 
     * @param localOnly should look it up locally only
     * @param uid the uid
     * @return the storage item
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    void copyItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    void moveItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Delete item.
     * 
     * @param uid the uid
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     */
    void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Stores item. Involves local storage only.
     * 
     * @param item the item
     * @throws StorageException the storage exception
     * @throws UnsupportedStorageOperationException the unsupported storage operation exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException;

    /**
     * Lists the path denoted by item.
     * 
     * @param uid the uid
     * @return the collection< storage item>
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    Collection<StorageItem> list( RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Lists the path denoted by item.
     * 
     * @param uid the uid
     * @return the collection< storage item>
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    Collection<StorageItem> list( StorageCollectionItem item )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Gets the target set for UID.
     * 
     * @param uid
     * @return
     */
    TargetSet getTargetsForRequest( RepositoryItemUid uid, Map<String, Object> context );

    /**
     * Creates an UID within this Repository.
     */
    RepositoryItemUid createUid( String path );

    /**
     * Will return the proper Action that will occur on "write" operation: create (if nothing exists on the given path)
     * or update (if overwrite will happen since the path already exists).
     * 
     * @param action
     * @return
     */
    Action getResultingActionOnWrite( ResourceStoreRequest rsr );
}
