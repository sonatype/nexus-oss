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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * This is default implementation of a repository. It supports age calculation, a repeated retrieval if item is found
 * locally but it's age is more then allowed.
 * 
 * @author cstamas
 */
public abstract class DefaultRepository
    extends AbstractRepository
{

    /**
     * The item max age.
     */
    private int itemMaxAge = 24 * 60;

    /**
     * Gets the item max age in (in minutes).
     * 
     * @return the item max age in (in minutes)
     */
    public int getItemMaxAge()
    {
        return itemMaxAge;
    }

    /**
     * Sets the item max age in (in minutes).
     * 
     * @param itemMaxAgeInSeconds the new item max age in (in minutes).
     */
    public void setItemMaxAge( int itemMaxAge )
    {
        this.itemMaxAge = itemMaxAge;
    }

    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem item = null;
        AbstractStorageItem localItem = null;
        AbstractStorageItem remoteItem = null;

        if ( getLocalStorage() != null )
        {
            try
            {
                localItem = getLocalStorage().retrieveItem( uid );
                localItem.getItemContext().putAll( context );
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Item " + uid.toString() + " found in local storage." );
                }
                // this "self correction" is needed to nexus build for himself the needed metadata
                if ( localItem.getLastTouched() == 0 )
                {
                    getLocalStorage().touchItem( localItem.getRepositoryItemUid() );
                    localItem = getLocalStorage().retrieveItem( uid );
                }
            }
            catch ( ItemNotFoundException ex )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Item " + uid.toString() + " not found in local storage." );
                }
                localItem = null;
            }
        }
        if ( getProxyMode() != null && getProxyMode().shouldProxy() && !localOnly )
        {
            // we are able to go remote
            if ( localItem == null || isOld( localItem ) )
            {
                // we should go remote coz we have no local copy or it is old
                try
                {
                    boolean shouldGetRemote = false;
                    if ( localItem != null )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger()
                                .debug( "Item " + uid.toString() + " is old, checking for newer file on remote." );
                        }

                        // check do we have newer remotely
                        shouldGetRemote = getRemoteStorage().containsItem( uid, localItem.getModified() );

                        if ( !shouldGetRemote )
                        {
                            // remote file unchanged, touch the local one to renew it's Age
                            getLocalStorage().touchItem( localItem.getRepositoryItemUid() );
                        }
                    }
                    else
                    {
                        // we have no local copy of it, try to get it unconditionally
                        shouldGetRemote = true;
                    }

                    if ( shouldGetRemote )
                    {
                        // this will GET it unconditionally
                        remoteItem = doRetrieveRemoteItem( uid, context );

                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Item " + uid.toString() + " found in remote storage." );
                        }
                    }
                    else
                    {
                        remoteItem = null;
                    }
                }
                catch ( ItemNotFoundException ex )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Item " + uid.toString() + " not found in remote storage." );
                    }
                    remoteItem = null;
                }
                catch ( StorageException ex )
                {
                    getLogger().warn(
                        "RemoteStorage of repository " + getId()
                            + " throws StorageException. Are we online? Is storage properly set up? ",
                        ex );
                    remoteItem = null;
                }
            }

            if ( localItem == null && remoteItem == null )
            {
                // we dont have neither one, NotFoundException
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger()
                        .debug(
                            "Item "
                                + uid.toString()
                                + " does not exist in local storage neither in remote storage, throwing ItemNotFoundException." );
                }
                throw new ItemNotFoundException( uid );
            }
            else if ( localItem != null && remoteItem == null )
            {
                // simple: we have local but not remote (coz we are offline or coz it is not newer)
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Item " + uid.toString() + " does exist in local storage and is fresh, returning local one." );
                }
                item = localItem;
            }
            else
            {
                // the fact that remoteItem != null means we _have_ to cache it
                // OR: we have to cache it if can, since we had no local copy
                // OR: we have to cache it if can, since remoteItem is fur sure newer (look above)
                item = remoteItem;
            }

        }
        else
        {
            // we cannot go remote
            if ( localItem != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Item " + uid.toString() + " does exist locally and cannot go remote, returning local one." );
                }
                item = localItem;
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Item " + uid.toString()
                            + " does not exist locally and cannot go remote, throwing ItemNotFoundException." );
                }
                throw new ItemNotFoundException( uid );
            }
        }
        return item;
    }

    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem result = getRemoteStorage().retrieveItem( uid );

        result.getItemContext().putAll( context );

        return doCacheItem( result );
    }

    protected AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws StorageException
    {
        AbstractStorageItem result = null;

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Caching item " + item.getRepositoryItemUid().toString() + " in local storage of repository." );
            }
            getLocalStorage().storeItem( item );
            removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );
            notifyProximityEventListeners( new RepositoryItemEventCache( item.getRepositoryItemUid(), item
                .getItemContext() ) );
            result = getLocalStorage().retrieveItem( item.getRepositoryItemUid() );
            result.getItemContext().putAll( item.getItemContext() );
        }
        catch ( ItemNotFoundException ex )
        {
            // this is a nonsense, we just stored it!
            result = item;
        }
        catch ( UnsupportedStorageOperationException ex )
        {
            getLogger().warn( "LocalStorage does not handle STORE operation, not caching remote fetched item.", ex );
            result = item;
        }

        return result;
    }

    protected void doCopyItem( RepositoryItemUid fromUid, RepositoryItemUid toUid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem item = getLocalStorage().retrieveItem( fromUid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                DefaultStorageFileItem target = new DefaultStorageFileItem(
                    this,
                    toUid.getPath(),
                    true,
                    true,
                    new PreparedContentLocator( ( (StorageFileItem) item ).getInputStream() ) );

                getLocalStorage().storeItem( target );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content of source file (is it file?)!", e );
            }
        }
    }

    protected void doDeleteItem( RepositoryItemUid uid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        getLocalStorage().deleteItem( uid );
    }

    protected Collection<StorageItem> doListItems( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        return getLocalStorage().listItems( uid );
    }

    /**
     * Checks if item is old with "default" maxAge.
     * 
     * @param item the item
     * @return true, if is old
     */
    protected boolean isOld( StorageItem item )
    {
        return isOld( getItemMaxAge(), item );
    }

    /**
     * Checks if item is old with given maxAge.
     * 
     * @param maxAge
     * @param item
     * @return
     */
    protected boolean isOld( int maxAge, StorageItem item )
    {
        // if item is manually expired, true
        if ( item.isExpired() )
        {
            return true;
        }

        // if repo is non-expirable, false
        if ( maxAge < 1 )
        {
            return false;
        }
        // else check age
        else
        {
            return ( ( System.currentTimeMillis() - item.getLastTouched() ) > ( maxAge * 60 * 1000 ) );
        }
    }

}
