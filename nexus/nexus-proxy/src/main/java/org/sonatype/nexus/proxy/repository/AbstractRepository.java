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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.EventMulticasterComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventClearCaches;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskActivityDescriptor;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskFilter;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

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
    protected static final long REMOTE_STATUS_RETAIN_TIME = 5L * 60L * 1000L;

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /**
     * The cache manager.
     */
    @Requirement
    private CacheManager cacheManager;

    /**
     * The target registry.
     */
    @Requirement
    private TargetRegistry targetRegistry;

    /**
     * Factory for UIDs.
     */
    @Requirement
    private RepositoryItemUidFactory repositoryItemUidFactory;

    /**
     * The access manager.
     */
    @Requirement
    private AccessManager accessManager;

    /**
     * The Walker service.
     */
    @Requirement
    private Walker walker;

    /** The id. */
    private String id;

    /** The local status */
    private volatile LocalStatus localStatus = LocalStatus.IN_SERVICE;

    /** The proxy mode */
    private volatile ProxyMode proxyMode = ProxyMode.ALLOW;

    /** The proxy remote status */
    private volatile RemoteStatus remoteStatus = RemoteStatus.UNKNOWN;

    /** The repo status check mode */
    private volatile RepositoryStatusCheckMode repositoryStatusCheckMode = RepositoryStatusCheckMode.NEVER;

    /** Last time remote status was updated */
    private volatile long remoteStatusUpdated = 0;

    /** The name. */
    private String name;

    /** The read only. */
    private boolean allowWrite = true;

    /** The listable. */
    private boolean browseable = true;

    /** The indexable. */
    private boolean indexable = true;

    /** User managed */
    private boolean userManaged = true;

    /** Exposed */
    private boolean exposed = true;

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

    /** Request processors list */
    private List<RequestProcessor> requestProcessors;

    /** Remote storage context to store connection configs. */
    private RemoteStorageContext remoteStorageContext;

    /**
     * The item max age.
     */
    private int itemMaxAge = 24 * 60;

    public void initialize()
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
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

    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        // TODO: now we are allowing everything. But this is not a finished implementation!
        return new DefaultRepositoryTaskFilter()
            .setAllowsRepositoryScanning( true ).setAllowsScheduledTasks( true ).setAllowsUserInitiatedTasks( true )
            .setContentOperators( DefaultRepositoryTaskActivityDescriptor.ALL_CONTENT_OPERATIONS )
            .setAttributeOperators( DefaultRepositoryTaskActivityDescriptor.ALL_ATTRIBUTES_OPERATIONS );
    }

    public LocalStatus getLocalStatus()
    {
        return localStatus;
    }

    public void setLocalStatus( LocalStatus localStatus )
    {
        LocalStatus oldStatus = this.localStatus;

        this.localStatus = localStatus;

        if ( !oldStatus.equals( localStatus ) )
        {
            notifyProximityEventListeners( new RepositoryEventLocalStatusChanged( this, oldStatus, localStatus ) );
        }
    }

    protected void resetRemoteStatus()
    {
        remoteStatusUpdated = 0;
    }

    protected boolean isRemoteStorageReachable()
        throws StorageException
    {
        if ( !RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            return false;
        }
        else
        {
            // TODO: include context? from where?
            return getRemoteStorage().isReachable( this, null );
        }

    }

    /** Is checking in progress? */
    private volatile boolean _remoteStatusChecking = false;

    public RemoteStatus getRemoteStatus( boolean forceCheck )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            // if the last known status is old, simply reset it
            if ( forceCheck || System.currentTimeMillis() - remoteStatusUpdated > REMOTE_STATUS_RETAIN_TIME )
            {
                remoteStatus = RemoteStatus.UNKNOWN;
            }

            if ( getProxyMode() != null && getProxyMode().shouldCheckRemoteStatus()
                && RemoteStatus.UNKNOWN.equals( remoteStatus ) && !_remoteStatusChecking )
            {
                // check for thread and go check it
                _remoteStatusChecking = true;

                exec.submit( new Callable<Object>()
                {
                    public Object call()
                        throws Exception
                    {
                        try
                        {
                            try
                            {
                                if ( isRemoteStorageReachable() )
                                {
                                    setRemoteStatus( RemoteStatus.AVAILABLE, null );
                                }
                                else
                                {
                                    setRemoteStatus( RemoteStatus.UNAVAILABLE, new ItemNotFoundException( "/" ) );
                                }
                            }
                            catch ( StorageException e )
                            {
                                setRemoteStatus( RemoteStatus.UNAVAILABLE, e );
                            }
                        }
                        finally
                        {
                            _remoteStatusChecking = false;
                        }

                        return null;
                    }
                } );
            }
            else if ( getProxyMode() != null && !getProxyMode().shouldCheckRemoteStatus()
                && RemoteStatus.UNKNOWN.equals( remoteStatus ) && !_remoteStatusChecking )
            {
                setRemoteStatus( RemoteStatus.UNAVAILABLE, null );

                _remoteStatusChecking = false;
            }

            return remoteStatus;
        }
        else
        {
            return null;
        }
    }

    private void setRemoteStatus( RemoteStatus remoteStatus, Throwable cause )
    {
        this.remoteStatus = remoteStatus;

        if ( RemoteStatus.AVAILABLE.equals( remoteStatus ) )
        {
            this.remoteStatusUpdated = System.currentTimeMillis();

            if ( getProxyMode() != null && getProxyMode().shouldAutoUnblock() )
            {
                setProxyMode( ProxyMode.ALLOW, true, cause );
            }
        }
        /*
         * AUTO_BLOCK temporarily disabled else if ( RemoteStatus.UNAVAILABLE.equals( remoteStatus ) ) {
         * this.remoteStatusUpdated = System.currentTimeMillis(); if ( getProxyMode() != null &&
         * getProxyMode().shouldProxy() ) { setProxyMode( ProxyMode.BLOCKED_AUTO, true, cause ); } }
         */
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

    protected void setProxyMode( ProxyMode proxyMode, boolean sendNotification, Throwable cause )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            ProxyMode oldProxyMode = this.proxyMode;

            this.proxyMode = proxyMode;

            // if this is proxy
            // and was !shouldProxy() and the new is shouldProxy()
            if ( this.proxyMode != null && this.proxyMode.shouldProxy() && !oldProxyMode.shouldProxy() )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "We have a !shouldProxy() -> shouldProxy() transition, purging NFC" );
                }

                getNotFoundCache().purge();

                resetRemoteStatus();
            }

            if ( sendNotification && !proxyMode.equals( oldProxyMode ) )
            {
                notifyProximityEventListeners( new RepositoryEventProxyModeChanged(
                    this,
                    oldProxyMode,
                    proxyMode,
                    cause ) );
            }
        }
    }

    public void setProxyMode( ProxyMode proxyMode )
    {
        setProxyMode( proxyMode, true, null );
    }

    protected void autoBlockProxying( Throwable cause )
    {
        if ( RepositoryType.PROXY.equals( getRepositoryType() ) )
        {
            setRemoteStatus( RemoteStatus.UNAVAILABLE, cause );
        }
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        return repositoryStatusCheckMode;
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        this.repositoryStatusCheckMode = mode;
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

    public boolean isUserManaged()
    {
        return userManaged;
    }

    public void setUserManaged( boolean userManaged )
    {
        this.userManaged = userManaged;
    }

    public boolean isExposed()
    {
        return exposed;
    }

    public void setExposed( boolean exposed )
    {
        this.exposed = exposed;
    }

    public List<RequestProcessor> getRequestProcessors()
    {
        if ( requestProcessors == null )
        {
            requestProcessors = new ArrayList<RequestProcessor>();
        }

        return requestProcessors;
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
        if ( remoteUrl == null )
        {
            this.remoteUrl = null;
        }
        else
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
    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public void setRemoteStorageContext( RemoteStorageContext remoteStorageContext )
    {
        this.remoteStorageContext = remoteStorageContext;

        if ( getProxyMode() != null && getProxyMode().shouldAutoUnblock() )
        {
            // perm changes? retry if autoBlocked
            setProxyMode( ProxyMode.ALLOW );
        }
    }

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

    protected Walker getWalker()
    {
        return walker;
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
                    // expire it
                    item.setExpired( true );

                    getLocalStorage().updateItemAttributes( item );
                }
                catch ( ItemNotFoundException e )
                {
                    // will not happen
                }
                catch ( StorageException e )
                {
                    getLogger().warn(
                        "Got storage exception while touching " + item.getRepositoryItemUid().toString(),
                        e );
                }
            }
        };

        // and let it loose
        walker.walk( path, true, false );

        notifyProximityEventListeners( new RepositoryEventClearCaches( this, path ) );
    }

    public Collection<String> evictUnusedItems( final long timestamp )
    {
        EvictUnusedItemsWalkerProcessor walkerProcessor = new EvictUnusedItemsWalkerProcessor( timestamp );

        DefaultWalkerContext ctx = new DefaultWalkerContext( this );

        ctx.getProcessors().add( walkerProcessor );

        // and let it loose
        walker.walk( ctx );

        notifyProximityEventListeners( new RepositoryEventEvictUnusedItems( this ) );

        return walkerProcessor.getFiles();
    }

    public boolean recreateAttributes( String path, final Map<String, String> initialData )
    {
        getLogger().info( "Rebuilding attributes on repository " + getId() );

        RecreateAttributesWalker walker = new RecreateAttributesWalker( this, getLogger(), initialData );

        walker.walk( path, true, false );

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

        setAllowWrite( remoteStorage == null );
    }

    // ===================================================================================
    // Store iface
    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, Action.read ) )
        {
            throw new ItemNotFoundException( request.getRequestPath(), this.getId() );
        }

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        StorageItem item = retrieveItem( request.isRequestLocalOnly(), uid, request.getRequestContext() );

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) && !isBrowseable() )
        {
            getLogger().debug(
                getId() + " retrieveItem() :: FOUND a collection on " + uid.toString()
                    + " but repository is not Browseable." );

            throw new ItemNotFoundException( uid );
        }

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
        if ( !checkConditions( from, Action.read ) )
        {
            throw new AccessDeniedException( from, "Operation does not fills needed requirements!" );
        }
        if ( !checkConditions( to, getResultingActionOnWrite( to ) ) )
        {
            throw new AccessDeniedException( to, "Operation does not fills needed requirements!" );
        }

        RepositoryItemUid fromUid = createUid( from.getRequestPath() );

        RepositoryItemUid toUid = createUid( to.getRequestPath() );

        copyItem( fromUid, toUid, to.getRequestContext() );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( from, Action.read ) )
        {
            throw new AccessDeniedException( from, "Operation does not fills needed requirements!" );
        }
        if ( !checkConditions( from, Action.delete ) )
        {
            throw new AccessDeniedException( from, "Operation does not fills needed requirements!" );
        }
        if ( !checkConditions( to, getResultingActionOnWrite( to ) ) )
        {
            throw new AccessDeniedException( to, "Operation does not fills needed requirements!" );
        }

        RepositoryItemUid fromUid = createUid( from.getRequestPath() );

        RepositoryItemUid toUid = createUid( to.getRequestPath() );

        moveItem( fromUid, toUid, to.getRequestContext() );
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
        if ( !checkConditions( request, Action.delete ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        deleteItem( uid, request.getRequestContext() );
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, getResultingActionOnWrite( request ) ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        DefaultStorageFileItem fItem = new DefaultStorageFileItem( this, request.getRequestPath(), true, true, is );

        fItem.getItemContext().putAll( request.getRequestContext() );

        if ( userAttributes != null )
        {
            fItem.getAttributes().putAll( userAttributes );
        }

        storeItem( fItem );
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, getResultingActionOnWrite( request ) ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

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
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, Action.read ) )
        {
            throw new ItemNotFoundException( request.getRequestPath(), this.getId() );
        }

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        Collection<StorageItem> items = null;

        if ( isBrowseable() )
        {
            items = list( uid, request.getRequestContext() );
        }
        else
        {
            throw new RepositoryNotListableException( this.getId() );
        }

        return items;
    }

    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        RepositoryItemUid uid = createUid( request.getRequestPath() );

        return getTargetsForRequest( uid, request.getRequestContext() );
    }

    public Action getResultingActionOnWrite( ResourceStoreRequest rsr )
    {
        try
        {
            RepositoryItemUid uid = createUid( rsr.getRequestPath() );

            retrieveItem( true, uid, rsr.getRequestContext() );

            return Action.update;
        }
        catch ( ItemNotFoundException e )
        {
            return Action.create;
        }
        catch ( StorageException e )
        {
            getLogger().warn( "Got exception while checking for resulting actionOnWrite", e );

            return null;
        }
        catch ( RepositoryNotAvailableException e )
        {
            getLogger().warn( "Got exception while checking for resulting actionOnWrite", e );

            return null;
        }
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

        getLocalStorage().touchItemLastRequested( uid );

        return getLocalStorage().retrieveItemContent( uid );
    }

    public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
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

        boolean isLocalOnlyRequest = ( localOnly ) || ( getProxyMode() != null && !getProxyMode().shouldProxy() );

        try
        {
            StorageItem item = null;

            repositoryItemUidFactory.lock( uid );

            try
            {
                item = doRetrieveItem( localOnly, uid, new HashMap<String, Object>() );
            }
            finally
            {
                repositoryItemUidFactory.unlock( uid );
            }

            if ( context != null )
            {
                item.getItemContext().putAll( context );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: FOUND " + uid.toString() );
            }

            notifyProximityEventListeners( new RepositoryItemEventRetrieve( item ) );

            return item;
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: NOT FOUND " + uid.toString() );
            }

            if ( !isLocalOnlyRequest )
            {
                addToNotFoundCache( uid.getPath() );
            }

            throw ex;
        }
    }

    public void copyItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
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

        StorageItem item = retrieveItem( true, from, context );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                DefaultStorageFileItem target = new DefaultStorageFileItem(
                    this,
                    to.getPath(),
                    true,
                    true,
                    new PreparedContentLocator( ( (StorageFileItem) item ).getInputStream() ) );

                target.getItemContext().putAll( item.getItemContext() );

                storeItem( target );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content of source file (is it file?)!", e );
            }
        }

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

        copyItem( from, to, context );

        deleteItem( from, context );
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
        StorageItem item = retrieveItem( true, uid, context );

        // fire the event for file being deleted
        notifyProximityEventListeners( new RepositoryItemEventDelete( item ) );

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "We are deleting a collection, starting a walker to send delete notifications per-file." );
            }

            // it is collection, walk it and below and fire events for all files
            DeletionNotifierWalker dnw = new DeletionNotifierWalker( this, getLogger(), context );

            dnw.walk( uid.getPath() );
        }

        doDeleteItem( uid );
    }

    public void storeItem( StorageItem item )
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

        // replace UID to own one
        item.setRepositoryItemUid( createUid( item.getPath() ) );

        // store it
        getLocalStorage().storeItem( item );

        // remove the "request" item from n-cache if there
        removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );

        notifyProximityEventListeners( new RepositoryItemEventStore( item ) );
    }

    public Collection<StorageItem> list( RepositoryItemUid uid, Map<String, Object> context )
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

        maintainNotFoundCache( uid.getPath() );

        Collection<StorageItem> items = doListItems( false, uid, context );

        for ( StorageItem item : items )
        {
            item.getItemContext().putAll( context );
        }

        return items;
    }

    public Collection<StorageItem> list( StorageCollectionItem item )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        return list( item.getRepositoryItemUid(), item.getItemContext() );
    }

    public TargetSet getTargetsForRequest( RepositoryItemUid uid, Map<String, Object> context )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "getTargetsForRequest() :: " + uid.toString() );
        }

        return targetRegistry.getTargetsForRepositoryPath( uid.getRepository(), uid.getPath() );
    }

    public RepositoryItemUid createUid( String path )
    {
        return repositoryItemUidFactory.createUid( this, path );
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
     * @return false, if the request should not be processed with response appropriate for current method, or true is
     *         execution should continue as usual.
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws AccessDeniedException the access denied exception
     */
    protected boolean checkConditions( ResourceStoreRequest request, Action action )
        throws RepositoryNotAvailableException,
            AccessDeniedException
    {
        return checkConditions( this, request, action );
    }

    protected boolean checkConditions( Repository repository, ResourceStoreRequest request, Action action )
        throws RepositoryNotAvailableException,
            AccessDeniedException
    {
        if ( !repository.getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( repository.getId() );
        }

        if ( !repository.isAllowWrite() && ( action.isWritingAction() ) )
        {
            throw new AccessDeniedException( request, "Repository with ID='" + repository.getId() + "' is Read Only!!" );
        }

        if ( isExposed() )
        {
            getAccessManager().decide( request, repository, action );
        }

        boolean result = true;

        if ( getRequestProcessors().size() > 0 )
        {
            for ( RequestProcessor processor : getRequestProcessors() )
            {
                result = result && processor.process( repository, request, action );
            }
        }

        return result;
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

            result = getLocalStorage().retrieveItem( item.getRepositoryItemUid() );

            notifyProximityEventListeners( new RepositoryItemEventCache( result ) );

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

    public boolean isCompatible( Repository repository )
    {
        return getRepositoryContentClass().isCompatible( repository.getRepositoryContentClass() );
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
    protected abstract Collection<StorageItem> doListItems( boolean localOnly, RepositoryItemUid uid,
        Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;
}
