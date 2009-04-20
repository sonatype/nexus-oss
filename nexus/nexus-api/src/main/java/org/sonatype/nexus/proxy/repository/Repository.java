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
package org.sonatype.nexus.proxy.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.ValidatingConfigurable;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

/**
 * Repository interface used by Proximity. It is an extension of ResourceStore iface, allowing to make direct
 * RepositoryItemUid based requests which bypasses AccessManager. Also, defines some properties.
 * 
 * @author cstamas
 */
public interface Repository
    extends ResourceStore, ValidatingConfigurable
{
    /**
     * Returns the ID of the resourceStore.
     * 
     * @return the id
     */
    String getId();

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
     * Used by router only, to specify a valid path prefix to a repository (previously was used getId() for this).
     * 
     * @return
     */
    String getPathPrefix();

    /**
     * Used by router only, to specify a valid path prefix to a repository (previously was used getId() for this).
     * 
     * @param prefix
     */
    void setPathPrefix( String prefix );

    /**
     * This is the "type"/kind of the repository. It tells some minimal info about the repo working (not content,
     * neither implementation).
     * 
     * @return
     */
    RepositoryKind getRepositoryKind();

    /**
     * This is the "class" of the repository content. It is used in grouping, only same content reposes may be grouped.
     * 
     * @return
     */
    ContentClass getRepositoryContentClass();

    /**
     * Returns the task filter for this repository.
     * 
     * @return
     */
    RepositoryTaskFilter getRepositoryTaskFilter();

    /**
     * Gets the target set for request.
     * 
     * @param uid
     * @return
     */
    TargetSet getTargetsForRequest( ResourceStoreRequest request );

    /**
     * Checks is there at all any target for the given request.
     * 
     * @param uid
     * @return
     */
    boolean hasAnyTargetsForRequest( ResourceStoreRequest request );

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

    /**
     * Is the target repository compatible to this one
     * 
     * @param repository
     * @return
     */
    boolean isCompatible( Repository repository );

    /**
     * Returns the facet of Repository, if available, otherwise it returns null.
     * 
     * @param <T>
     * @param t
     * @return the facet requested, otherwise null.
     */
    <F> F adaptToFacet( Class<F> t );

    // ==================================================
    // NFC et al

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
     * Maintains NFC.
     * 
     * @param path
     * @throws ItemNotFoundException
     */
    void maintainNotFoundCache( String path )
        throws ItemNotFoundException;

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
     * Is NFC active? (true by default)
     * 
     * @return
     */
    boolean isNotFoundCacheActive();

    /**
     * Sets is NFC active.
     * 
     * @param notFoundCacheActive
     */
    void setNotFoundCacheActive( boolean notFoundCacheActive );

    // ==================================================
    // LocalStorage et al

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
    void setLocalUrl( String url )
        throws StorageException;

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
     * Gets the published mirrors.
     * 
     * @return
     */
    PublishedMirrors getPublishedMirrors();

    // ==================================================
    // Behaviour

    /**
     * Returns the list of defined request processors.
     * 
     * @return
     */
    List<RequestProcessor> getRequestProcessors();

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

    // ==================================================
    // Maintenance

    /**
     * Purges the caches (NFC and expires files) from path and below.
     * 
     * @param path a path from to start descending. If null, it is taken as "root".
     */
    void clearCaches( ResourceStoreRequest request );

    /**
     * Purges the NFC caches from path and below.
     * 
     * @param path
     */
    void clearNotFoundCaches( ResourceStoreRequest request );

    /**
     * Evicts items that were last used before timestamp.
     * 
     * @param timestamp
     */
    Collection<String> evictUnusedItems( ResourceStoreRequest request, long timestamp );

    /**
     * Forces the recreation of attributes on this repository.
     * 
     * @param initialData the initial data
     * @return true, if recreate attributes
     */
    boolean recreateAttributes( ResourceStoreRequest request, Map<String, String> initialData );

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

    // ==================================================
    // Alternative (and unprotected) Content access

    StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException;

    void copyItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException;

    void moveItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException;

    void deleteItem( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException;

    void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException;

    Collection<StorageItem> list( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException;

    Collection<StorageItem> list( boolean fromTask, StorageCollectionItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException;
}
