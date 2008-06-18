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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.EventMulticasterComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.OpenAccessManager;
import org.sonatype.nexus.proxy.access.RepositoryPermission;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.RepositoryEventClearCaches;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeBlockedAutomatically;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;
import org.sonatype.scheduling.Scheduler;

/**
 * <p>
 * A common base for Proximity repository. It defines all the needed properties and main methods as in
 * ProximityRepository interface.
 * <p>
 * This abstract class handles the following functionalities:
 * <ul>
 * <li>Holds base properties like repo ID, group ID, rank</li>
 * <li>Manages AccessManager</li>
 * <li>Manages notFoundCache to speed up responses</li>
 * <li>Manages event listeners</li>
 * </ul>
 * <p>
 * The subclasses only needs to implement the abstract method focusing on item retrieaval and other "basic" functions.
 * 
 * @author cstamas
 */
public abstract class AbstractRepository
    extends EventMulticasterComponent
    implements Repository, Initializable
{
    /** The time while we do NOT check an already known remote status: 5 mins */
    private static final long REMOTE_STATUS_RETAIN_TIME = 5 * 60 * 1000;

    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * The cache manager.
     * 
     * @plexus.requirement
     */
    private CacheManager cacheManager;

    /**
     * The Scheduler.
     * 
     * @plexus.requirement
     */
    private Scheduler scheduler;

    /** The id. */
    private String id;

    /** The local status */
    private LocalStatus localStatus = LocalStatus.IN_SERVICE;

    /** The proxy mode */
    private ProxyMode proxyMode = ProxyMode.ALLOW;

    /** The proxy remote status */
    private RemoteStatus remoteStatus = RemoteStatus.UNKNOWN;

    /** Last time remote status was updated */
    private long remoteStatusUpdated = 0;

    /** Is checking in progress? */
    private boolean remoteStatusChecking = false;

    /** The name. */
    private String name;

    /** The read only. */
    private boolean allowWrite = true;

    /** The listable. */
    private boolean browseable = true;

    /** The indexable. */
    private boolean indexable = true;

    /** The access manager. */
    private AccessManager accessManager = new OpenAccessManager();

    /** The local storage. */
    private LocalRepositoryStorage localStorage;

    /** The remote storage. */
    private RemoteRepositoryStorage remoteStorage;

    /** The not found cache. */
    private PathCache notFoundCache;

    /** The not found cache time to live (in minutes). */
    private int notFoundCacheTimeToLive = 24 * 60;

    /** The local url. */
    private String localUrl;

    /** The remote url. */
    private String remoteUrl;

    /** Remote storage context to store connection configs. */
    private RemoteStorageContext remoteStorageContext;

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        // TODO
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public RepositoryType getRepositoryType()
    {
        if ( getRemoteUrl() != null )
        {
            return RepositoryType.PROXY;
        }
        else
        {
            return RepositoryType.HOSTED;
        }
    }

    public LocalStatus getLocalStatus()
    {
        return localStatus;
    }

    public void setLocalStatus( LocalStatus localStatus )
    {
        LocalStatus oldStatus = getLocalStatus();

        this.localStatus = localStatus;

        notifyProximityEventListeners( new RepositoryEventLocalStatusChanged( this, oldStatus ) );
    }

    public RemoteStatus getRemoteStatus( boolean forceCheck )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            // if the last known status is old, simply reset it
            if ( forceCheck || System.currentTimeMillis() - remoteStatusUpdated > REMOTE_STATUS_RETAIN_TIME )
            {
                remoteStatus = RemoteStatus.UNKNOWN;

                remoteStatusUpdated = System.currentTimeMillis();
            }

            if ( getProxyMode() != null && getProxyMode().shouldProxy() && RemoteStatus.UNKNOWN.equals( remoteStatus )
                && !remoteStatusChecking )
            {
                // check for thread and go check it
                scheduler.submit( getId() + " remote status check", new Callable<Object>()
                {
                    public Object call()
                        throws Exception
                    {
                        remoteStatusChecking = true;

                        try
                        {
                            if ( isRemoteStorageReachable() )
                            {
                                remoteStatus = RemoteStatus.AVAILABLE;
                            }
                            else
                            {
                                remoteStatus = RemoteStatus.UNAVAILABLE;
                            }
                        }
                        finally
                        {
                            remoteStatusChecking = false;
                        }

                        return null;
                    }
                }, null );
            }
            else if ( getProxyMode() != null && !getProxyMode().shouldProxy()
                && RemoteStatus.UNKNOWN.equals( remoteStatus ) && !remoteStatusChecking )
            {
                remoteStatus = RemoteStatus.UNAVAILABLE;

                remoteStatusChecking = false;
            }

            return remoteStatus;
        }
        else
        {
            return null;
        }
    }

    public ProxyMode getProxyMode()
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            return proxyMode;
        }
        else
        {
            return null;
        }
    }

    public void setProxyMode( ProxyMode proxyMode )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            // if this is proxy
            // and was !shouldProxy() and the new is shouldProxy()
            // if ( this.proxyMode != null && !this.proxyMode.shouldProxy() && proxyMode.shouldProxy() )
            // the line above is CORRECT way to doing it, BUT until models are not used
            // properly and we are UPDATING instead of RECREATING reposes, the below works
            if ( proxyMode.shouldProxy() )
            {
                getNotFoundCache().purge();
            }
        }

        this.proxyMode = proxyMode;
    }

    protected void autoBlockProxying( Throwable cause )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            ProxyMode oldMode = getProxyMode();

            setProxyMode( ProxyMode.BLOCKED_AUTO );

            notifyProximityEventListeners( new RepositoryEventProxyModeBlockedAutomatically( this, oldMode, cause ) );
        }
    }

    public boolean isAllowWrite()
    {
        return allowWrite;
    }

    public void setAllowWrite( boolean allowWrite )
    {
        this.allowWrite = allowWrite;
    }

    public boolean isBrowseable()
    {
        return browseable;
    }

    public void setBrowseable( boolean browseable )
    {
        this.browseable = browseable;
    }

    public int getNotFoundCacheTimeToLive()
    {
        return notFoundCacheTimeToLive;
    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        this.notFoundCacheTimeToLive = notFoundCacheTimeToLive;
    }

    /**
     * Gets the cache manager.
     * 
     * @return the cache manager
     */
    public CacheManager getCacheManager()
    {
        return cacheManager;
    }

    /**
     * Sets the cache manager.
     * 
     * @param cacheManager the new cache manager
     */
    public void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    /**
     * Gets the not found cache.
     * 
     * @return the not found cache
     */
    public PathCache getNotFoundCache()
    {
        if ( notFoundCache == null )
        {
            notFoundCache = getCacheManager().getPathCache( getId() );
        }
        return notFoundCache;
    }

    /**
     * Sets the not found cache.
     * 
     * @param notFoundcache the new not found cache
     */
    public void setNotFoundCache( PathCache notFoundcache )
    {
        this.notFoundCache = notFoundcache;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public boolean isIndexable()
    {
        return indexable;
    }

    public void setIndexable( boolean indexable )
    {
        this.indexable = indexable;
    }

    public String getLocalUrl()
    {
        return localUrl;
    }

    public void setLocalUrl( String localUrl )
    {
        String trstr = localUrl.trim();
        if ( !trstr.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            this.localUrl = trstr;
        }
        else
        {
            this.localUrl = trstr.substring( 0, trstr.length() - 1 );
        }
    }

    public String getRemoteUrl()
    {
        return remoteUrl;
    }

    public void setRemoteUrl( String remoteUrl )
    {
        String trstr = remoteUrl.trim();

        if ( !trstr.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            this.remoteUrl = trstr;
        }
        else
        {
            this.remoteUrl = trstr.substring( 0, trstr.length() - 1 );
        }
    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public void setRemoteStorageContext( RemoteStorageContext remoteStorageContext )
    {
        this.remoteStorageContext = remoteStorageContext;
    }

    // ===================================================================================
    // Repository iface

    public AccessManager getAccessManager()
    {
        return accessManager;
    }

    public void setAccessManager( AccessManager accessManager )
    {
        this.accessManager = accessManager;
    }

    public boolean isRemoteStorageReachable()
    {
        if ( !RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            return false;
        }
        else
        {
            try
            {
                return getRemoteStorage().isReachable( this );
            }
            catch ( StorageException e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "isRemoteStorageReachable :: StorageException", e );
                }

                autoBlockProxying( e );

                return false;
            }
        }

    }

    public void clearCaches( String path )
    {
        // no path given, purge the whole NFC
        if ( path == null || RepositoryItemUid.PATH_ROOT.equals( path ) )
        {
            path = RepositoryItemUid.PATH_ROOT;

            if ( getNotFoundCache() != null )
            {
                getNotFoundCache().purge();
            }
        }
        else
        {
            if ( getNotFoundCache() != null )
            {
                getNotFoundCache().removeWithParents( path );

                getNotFoundCache().removeWithChildren( path );
            }
        }

        // construct an imperial walker to do the job
        StoreFileWalker walker = new StoreFileWalker( this, getLogger() )
        {
            @Override
            protected void processFileItem( StorageFileItem item )
            {
                // expiring found files
                try
                {
                    // set this "very old", but not 0!!!
                    getLocalStorage().touchItem( item.getRepositoryItemUid(), AbstractStorageItem.EXPIRED_TS );
                }
                catch ( ItemNotFoundException e )
                {
                    // will not happen
                }
                catch ( StorageException e )
                {
                    logger.warn( "Got storage exception while touching " + item.getRepositoryItemUid().toString(), e );
                }
            }
        };

        // and let it loose
        walker.walk( path, true, false );

        notifyProximityEventListeners( new RepositoryEventClearCaches( this, path ) );
    }

    public void evictUnusedItems( final long timestamp )
    {
        // construct an imperial walker to do the job
        StoreFileWalker walker = new StoreFileWalker( this, getLogger() )
        {
            @Override
            protected void processFileItem( StorageFileItem item )
            {
                // expiring found files
                try
                {
                    if ( item.getLastTouched() < timestamp )
                    {
                        deleteItem( item.getRepositoryItemUid() );
                    }
                }
                catch ( RepositoryNotAvailableException e )
                {
                    // simply stop if set during processing
                    stop();
                }
                catch ( UnsupportedStorageOperationException e )
                {
                    // if op not supported (R/O repo?)
                    stop();
                }
                catch ( ItemNotFoundException e )
                {
                    // will not happen
                }
                catch ( StorageException e )
                {
                    logger.warn( "Got storage exception while evicting " + item.getRepositoryItemUid().toString(), e );
                }
            }

            @Override
            protected void onCollectionExit( StorageCollectionItem coll )
            {
                // expiring now empty directories
                try
                {
                    if ( coll.list().size() == 0 )
                    {
                        deleteItem( coll.getRepositoryItemUid() );
                    }
                }
                catch ( RepositoryNotAvailableException e )
                {
                    // simply stop if set during processing
                    stop();
                }
                catch ( UnsupportedStorageOperationException e )
                {
                    // if op not supported (R/O repo?)
                    stop();
                }
                catch ( ItemNotFoundException e )
                {
                    // will not happen
                }
                catch ( StorageException e )
                {
                    logger.warn( "Got storage exception while evicting " + coll.getRepositoryItemUid().toString(), e );
                }
            }
        };

        // and let it loose
        walker.walk( RepositoryItemUid.PATH_ROOT, true, false );

        notifyProximityEventListeners( new RepositoryEventEvictUnusedItems( this ) );
    }

    public boolean recreateAttributes( final Map<String, String> initialData )
    {
        getLogger().info( "Recreating attributes on repository " + getId() );

        RecreateAttributesWalker walker = new RecreateAttributesWalker( this, getLogger(), initialData );

        walker.walk( true, false );

        notifyProximityEventListeners( new RepositoryEventRecreateAttributes( this ) );

        return true;
    }

    public LocalRepositoryStorage getLocalStorage()
    {
        return localStorage;
    }

    public void setLocalStorage( LocalRepositoryStorage localStorage )
    {
        this.localStorage = localStorage;
    }

    public RemoteRepositoryStorage getRemoteStorage()
    {
        return remoteStorage;
    }

    public void setRemoteStorage( RemoteRepositoryStorage remoteStorage )
    {
        this.remoteStorage = remoteStorage;

        setAllowWrite( false );
    }

    // ===================================================================================
    // Store iface

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( request, RepositoryPermission.RETRIEVE );

        RepositoryItemUid uid = new RepositoryItemUid( this, request.getRequestPath() );

        StorageItem item = retrieveItem( request.isRequestLocalOnly(), uid );

        item.getItemContext().putAll( request.getRequestContext() );

        notifyProximityEventListeners( new RepositoryItemEventRetrieve( item.getRepositoryItemUid(), item
            .getItemContext() ) );

        return item;
    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( from, RepositoryPermission.RETRIEVE );

        checkConditions( to, RepositoryPermission.STORE );

        RepositoryItemUid fromUid = new RepositoryItemUid( this, from.getRequestPath() );

        RepositoryItemUid toUid = new RepositoryItemUid( this, to.getRequestPath() );

        copyItem( fromUid, toUid );

        notifyProximityEventListeners( new RepositoryItemEventStore( toUid, to.getRequestContext() ) );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( from, RepositoryPermission.RETRIEVE );

        checkConditions( from, RepositoryPermission.DELETE );

        checkConditions( to, RepositoryPermission.STORE );

        RepositoryItemUid fromUid = new RepositoryItemUid( this, from.getRequestPath() );

        RepositoryItemUid toUid = new RepositoryItemUid( this, to.getRequestPath() );

        notifyProximityEventListeners( new RepositoryItemEventDelete( fromUid, from.getRequestContext() ) );

        moveItem( fromUid, toUid, to.getRequestContext() );

        notifyProximityEventListeners( new RepositoryItemEventStore( toUid, to.getRequestContext() ) );
    }

    public void deleteItem( ResourceStoreRequest request )
        throws IllegalArgumentException,
            UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( request, RepositoryPermission.DELETE );

        RepositoryItemUid uid = new RepositoryItemUid( this, request.getRequestPath() );

        // fire the event for file being deleted
        notifyProximityEventListeners( new RepositoryItemEventDelete( uid, request.getRequestContext() ) );

        deleteItem( uid, request.getRequestContext() );
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( request, RepositoryPermission.STORE );

        DefaultStorageFileItem fItem = new DefaultStorageFileItem( this, request.getRequestPath(), true, true, is );

        fItem.getItemContext().putAll( request.getRequestContext() );

        if ( userAttributes != null )
        {
            fItem.getAttributes().putAll( userAttributes );
        }

        storeItem( fItem );

        notifyProximityEventListeners( new RepositoryItemEventStore( fItem.getRepositoryItemUid(), fItem
            .getItemContext() ) );
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( request, RepositoryPermission.STORE );

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
            this,
            request.getRequestPath(),
            true,
            true );

        coll.getItemContext().putAll( request.getRequestContext() );

        if ( userAttributes != null )
        {
            coll.getAttributes().putAll( userAttributes );
        }

        storeItem( coll );

        notifyProximityEventListeners( new RepositoryItemEventStore( coll.getRepositoryItemUid(), coll.getItemContext() ) );
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkConditions( request, RepositoryPermission.LIST );

        RepositoryItemUid uid = new RepositoryItemUid( this, request.getRequestPath() );

        Collection<StorageItem> items = list( uid );

        for ( StorageItem item : items )
        {
            item.getItemContext().putAll( request.getRequestContext() );
        }

        return items;
    }

    // ===================================================================================
    // Repositry store-like

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "retrieveItemContent() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        return getLocalStorage().retrieveItemContent( uid );
    }

    public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "retrieveItem() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        maintainNotFoundCache( uid.getPath() );

        boolean isCollectionAndBrowsingNotAllowed = false;

        boolean isLocalOnlyRequest = ( localOnly ) || ( getProxyMode() != null && !getProxyMode().shouldProxy() );

        try
        {
            StorageItem item = doRetrieveItem( localOnly, uid, new HashMap<String, Object>() );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: FOUND " + uid.toString() );
            }

            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) && !isBrowseable() )
            {
                getLogger().debug(
                    getId() + " retrieveItem() :: FOUND a collection on " + uid.toString()
                        + " but repository is not Browseable." );

                isCollectionAndBrowsingNotAllowed = true;

                throw new ItemNotFoundException( uid );
            }

            return item;
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: NOT FOUND " + uid.toString() );
            }

            if ( !isCollectionAndBrowsingNotAllowed && !isLocalOnlyRequest )
            {
                addToNotFoundCache( uid.getPath() );
            }

            throw ex;
        }
    }

    public void copyItem( RepositoryItemUid from, RepositoryItemUid to )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "copyItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        maintainNotFoundCache( from.getPath() );

        doCopyItem( from, to );

        // remove the "to" item from n-cache if there
        removeFromNotFoundCache( to.getPath() );
    }

    public void moveItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "moveItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        copyItem( from, to );

        deleteItem( from, context );
    }

    public void moveItem( RepositoryItemUid from, RepositoryItemUid to )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        moveItem( from, to, null );
    }

    public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItem() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        maintainNotFoundCache( uid.getPath() );

        // determine is the thing to be deleted a collection or not
        StorageItem item = retrieveItem( true, uid );

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            // it is collection, walk it and below and fire events for all files
            DeletionNotifierWalker dnw = new DeletionNotifierWalker( this, getLogger(), context );

            dnw.walk( uid.getPath() );
        }

        doDeleteItem( uid );
    }

    public void deleteItem( RepositoryItemUid uid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        deleteItem( uid, null );
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "storeItem() :: " + item.getRepositoryItemUid().toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        getLocalStorage().storeItem( item );

        // remove the "request" item from n-cache if there
        removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );
    }

    public Collection<StorageItem> list( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "list() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }

        if ( isBrowseable() )
        {
            maintainNotFoundCache( uid.getPath() );

            return doListItems( uid );
        }
        else
        {
            // empty list, we are not listable
            return Collections.emptyList();
        }
    }

    // ===================================================================================
    // Inner stuff

    /**
     * Maintains not found cache.
     * 
     * @param path the path
     * @throws ItemNotFoundException the item not found exception
     */
    protected void maintainNotFoundCache( String path )
        throws ItemNotFoundException
    {
        if ( getNotFoundCache() != null )
        {
            if ( getNotFoundCache().contains( path ) )
            {
                if ( getNotFoundCache().isExpired( path ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "The path " + path + " is in NFC but expired." );
                    }
                    removeFromNotFoundCache( path );
                }
                else
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "The path " + path + " is in NFC and still active, throwing ItemNotFoundException." );
                    }
                    throw new ItemNotFoundException( path );
                }
            }
        }
    }

    /**
     * Adds the uid to not found cache.
     * 
     * @param path the path
     */
    public void addToNotFoundCache( String path )
    {
        if ( getNotFoundCache() != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Adding path " + path + " to NFC." );
            }
            getNotFoundCache().put( path, Boolean.TRUE, getNotFoundCacheTimeToLive() * 60 );
        }
    }

    /**
     * Removes the uid from not found cache.
     * 
     * @param path the path
     */
    public void removeFromNotFoundCache( String path )
    {
        if ( getNotFoundCache() != null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Removing path " + path + " from NFC." );
            }
            getNotFoundCache().removeWithParents( path );
        }
    }

    /**
     * Check conditions, such as availability, permissions, etc.
     * 
     * @param request the request
     * @param permission the permission
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    protected void checkConditions( ResourceStoreRequest request, RepositoryPermission permission )
        throws RepositoryNotAvailableException,
            AccessDeniedException
    {
        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this.getId() );
        }
        if ( !isAllowWrite() && ( RepositoryPermission.STORE.equals( permission ) ) )
        {
            throw new AccessDeniedException(
                new RepositoryItemUid( this, request.getRequestPath() ),
                "Repository is READ ONLY!!" );
        }
        getAccessManager().decide( request, this, permission );
    }

    /**
     * Do retrieve item.
     * 
     * @param request the request
     * @param uid the uid
     * @return the storage item
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Do copy item.
     * 
     * @param from the from
     * @param fromUid the from uid
     * @param to the to
     * @param toUid the to uid
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws UnsupportedStorageOperationException
     */
    protected abstract void doCopyItem( RepositoryItemUid fromUid, RepositoryItemUid toUid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Do delete item.
     * 
     * @param request the request
     * @param uid the uid
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws UnsupportedStorageOperationException
     */
    protected abstract void doDeleteItem( RepositoryItemUid uid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

    /**
     * Do list items.
     * 
     * @param uid the uid
     * @return the collection< storage item>
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract Collection<StorageItem> doListItems( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;

}
