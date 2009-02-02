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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.EventMulticasterComponent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
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
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
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
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskActivityDescriptor;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskFilter;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;
import org.sonatype.nexus.util.ContextUtils;

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
    /**
     * StorageItem context key. If value set to Boolean.TRUE, the item will not be stored locally. Useful to suppress
     * caching of secondary items, like merged m2 group repository metadata.
     */
    public static final String CTX_TRANSITIVE_ITEM = AbstractRepository.class.getCanonicalName()
        + ".CTX_TRANSITIVE_ITEM";

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

    /**
     * The known ContentGenerators.
     */
    @Requirement( role = ContentGenerator.class )
    private Map<String, ContentGenerator> contentGenerators;

    /** The id. */
    private String id;

    /** The name. */
    private String name;

    /** The local status */
    private volatile LocalStatus localStatus = LocalStatus.IN_SERVICE;

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

    /** The not found cache. */
    private PathCache notFoundCache;

    /** Flag: is NFC active */
    private boolean notFoundCacheActive = true;

    /** The not found cache time to live (in minutes). */
    private int notFoundCacheTimeToLive = 24 * 60;

    /** The local url. */
    private String localUrl;

    /** Request processors list */
    private List<RequestProcessor> requestProcessors;

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

    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        // we are allowing all, and subclasses will filter as they want
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
            // getting it lazily
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

    public boolean isNotFoundCacheActive()
    {
        return notFoundCacheActive;
    }

    public void setNotFoundCacheActive( boolean notFoundCacheActive )
    {
        this.notFoundCacheActive = notFoundCacheActive;
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

    @SuppressWarnings( "unchecked" )
    public <T> T adaptToFacet( Class<T> t )
    {
        if ( t.isAssignableFrom( this.getClass() ) )
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    protected Walker getWalker()
    {
        return walker;
    }

    protected Map<String, ContentGenerator> getContentGenerators()
    {
        return contentGenerators;
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
        if ( StringUtils.isEmpty( path ) )
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        // 1st, expire all the files below path
        DefaultWalkerContext ctx = new DefaultWalkerContext( this );

        ctx.getProcessors().add( new ClearCacheWalker( this ) );

        walker.walk( ctx, path );

        // 2nd, remove the items from NFC
        if ( RepositoryItemUid.PATH_ROOT.equals( path ) )
        {
            // purge all
            path = RepositoryItemUid.PATH_ROOT;

            if ( getNotFoundCache() != null )
            {
                getNotFoundCache().purge();
            }
        }
        else
        {
            // purge below and above path only
            if ( getNotFoundCache() != null )
            {
                getNotFoundCache().removeWithParents( path );

                getNotFoundCache().removeWithChildren( path );
            }
        }

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

        RecreateAttributesWalker walkerProcessor = new RecreateAttributesWalker( this, initialData );

        DefaultWalkerContext ctx = new DefaultWalkerContext( this );

        ctx.getProcessors().add( walkerProcessor );

        walker.walk( ctx, path );

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

    // ===================================================================================
    // Store iface

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, Action.read ) )
        {
            throw new ItemNotFoundException( request.getRequestPath(), this.getId() );
        }

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        StorageItem item = retrieveItem( uid, request.getRequestContext() );

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
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( from, Action.read ) )
        {
            throw new IllegalRequestException( from, "copyItem: Operation does not fills needed requirements!" );
        }
        if ( !checkConditions( to, getResultingActionOnWrite( to ) ) )
        {
            throw new IllegalRequestException( to, "copyItem: Operation does not fills needed requirements!" );
        }

        RepositoryItemUid fromUid = createUid( from.getRequestPath() );

        RepositoryItemUid toUid = createUid( to.getRequestPath() );

        copyItem( fromUid, toUid, to.getRequestContext() );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
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
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
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
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        if ( !checkConditions( request, getResultingActionOnWrite( request ) ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        DefaultStorageFileItem fItem = new DefaultStorageFileItem(
            this,
            request.getRequestPath(),
            true,
            true,
            new PreparedContentLocator( is ) );

        fItem.getItemContext().putAll( request.getRequestContext() );

        if ( userAttributes != null )
        {
            fItem.getAttributes().putAll( userAttributes );
        }

        storeItem( fItem );
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
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
        throws IllegalOperationException,
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
            throw new RepositoryNotListableException( this );
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

            retrieveItem( uid, rsr.getRequestContext() );

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
        catch ( IllegalOperationException e )
        {
            getLogger().warn( "Got exception while checking for resulting actionOnWrite", e );

            return null;
        }
    }

    // ===================================================================================
    // Repositry store-like

    public StorageItem retrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "retrieveItem() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        ContextUtils.collAdd( context, ResourceStoreRequest.CTX_PROCESSED_REPOSITORIES, this.getId() );

        maintainNotFoundCache( uid.getPath() );

        try
        {
            StorageItem item = null;

            repositoryItemUidFactory.lock( uid );

            try
            {
                item = doRetrieveItem( uid, context );
            }
            finally
            {
                repositoryItemUidFactory.unlock( uid );
            }

            // Dyna content?
            if ( item instanceof StorageFileItem
                && item.getAttributes().containsKey( ContentGenerator.CONTENT_GENERATOR_ID ) )
            {
                StorageFileItem file = (StorageFileItem) item;

                String key = file.getAttributes().get( ContentGenerator.CONTENT_GENERATOR_ID );

                if ( getContentGenerators().containsKey( key ) )
                {
                    ContentGenerator generator = getContentGenerators().get( key );

                    file.setContentLocator( generator.generateContent( this, uid.getPath(), file ) );
                }
                else
                {
                    getLogger().info(
                        "The file in repository ID='" + this.getId() + "' on path='" + uid.getPath()
                            + "' should be generated by ContentGeneratorId='" + key + "' but it is not found!" );

                    throw new ItemNotFoundException( uid );
                }
            }

            notifyProximityEventListeners( new RepositoryItemEventRetrieve( this, item ) );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: FOUND " + uid.toString() );
            }

            return item;
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( getId() + " retrieveItem() :: NOT FOUND " + uid.toString() );
            }

            addToNotFoundCache( uid.getPath() );

            throw ex;
        }
    }

    public void copyItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "copyItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        maintainNotFoundCache( from.getPath() );

        StorageItem item = retrieveItem( from, context );

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
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "moveItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        copyItem( from, to, context );

        deleteItem( from, context );
    }

    public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItem() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        maintainNotFoundCache( uid.getPath() );

        // determine is the thing to be deleted a collection or not
        StorageItem item = getLocalStorage().retrieveItem( this, context, uid.getPath() );

        if ( context != null )
        {
            item.getItemContext().putAll( context );
        }

        // fire the event for file being deleted
        notifyProximityEventListeners( new RepositoryItemEventDelete( this, item ) );

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "We are deleting a collection, starting a walker to send delete notifications per-file." );
            }

            // it is collection, walk it and below and fire events for all files
            DeletionNotifierWalker dnw = new DeletionNotifierWalker( this, context );

            DefaultWalkerContext ctx = new DefaultWalkerContext( this );

            ctx.getProcessors().add( dnw );

            walker.walk( ctx, uid.getPath() );
        }

        doDeleteItem( uid, context );
    }

    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "storeItem() :: " + item.getRepositoryItemUid().toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        // replace UID to own one
        item.setRepositoryItemUid( createUid( item.getPath() ) );

        // store it
        getLocalStorage().storeItem( this, item.getItemContext(), item );

        // remove the "request" item from n-cache if there
        removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );

        notifyProximityEventListeners( new RepositoryItemEventStore( this, item ) );
    }

    public Collection<StorageItem> list( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "list() :: " + uid.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        ContextUtils.collAdd( context, ResourceStoreRequest.CTX_PROCESSED_REPOSITORIES, this.getId() );

        maintainNotFoundCache( uid.getPath() );

        Collection<StorageItem> items = doListItems( uid, context );

        if ( context != null )
        {
            for ( StorageItem item : items )
            {
                item.getItemContext().putAll( context );
            }
        }

        return items;
    }

    public Collection<StorageItem> list( StorageCollectionItem item )
        throws IllegalOperationException,
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
        if ( isNotFoundCacheActive() )
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
        if ( isNotFoundCacheActive() )
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
        if ( isNotFoundCacheActive() )
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
        throws IllegalOperationException,
            AccessDeniedException
    {
        return checkConditions( this, request, action );
    }

    protected boolean checkConditions( Repository repository, ResourceStoreRequest request, Action action )
        throws IllegalOperationException,
            AccessDeniedException
    {
        if ( !repository.getLocalStatus().shouldServiceRequest() )
        {
            throw new IllegalRequestException( request, "Repository with ID='" + repository.getId()
                + "' is not available (localStatus=" + repository.getLocalStatus().toString() + ")!" );
        }

        if ( !repository.isAllowWrite() && ( action.isWritingAction() ) )
        {
            throw new IllegalRequestException( request, "Repository with ID='" + repository.getId()
                + "' is Read Only, but action was '" + action.toString() + "'!" );
        }

        if ( isExposed() )
        {
            getAccessManager().decide( request, repository, action );
        }

        boolean shouldProcess = true;

        if ( getRequestProcessors().size() > 0 )
        {
            for ( RequestProcessor processor : getRequestProcessors() )
            {
                shouldProcess = shouldProcess && processor.process( repository, request, action );
            }
        }

        return shouldProcess;
    }

    public boolean isCompatible( Repository repository )
    {
        return getRepositoryContentClass().isCompatible( repository.getRepositoryContentClass() );
    }

    protected AbstractStorageItem createStorageItem( RepositoryItemUid uid, byte[] bytes, Map<String, Object> context )
    {
        ContentLocator content = new ByteArrayContentLocator( bytes );

        DefaultStorageFileItem result = new DefaultStorageFileItem(
            this,
            uid.getPath(),
            true /* isReadable */,
            false /* isWritable */,
            content );
        result.getItemContext().putAll( context );
        result.setMimeType( "text/plain" );
        result.setLength( bytes.length );

        return result;
    }

    protected void doDeleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            StorageException
    {
        getLocalStorage().deleteItem( this, context, uid.getPath() );
    }

    protected Collection<StorageItem> doListItems( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        return getLocalStorage().listItems( this, context, uid.getPath() );
    }

    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem localItem = null;

        try
        {
            localItem = getLocalStorage().retrieveItem( this, context, uid.getPath() );

            if ( context != null )
            {
                localItem.getItemContext().putAll( context );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Item " + uid.toString() + " found in local storage." );
            }

            // this "self correction" is needed to nexus build for himself the needed metadata
            if ( localItem.getRemoteChecked() == 0 )
            {
                getLocalStorage().touchItemRemoteChecked( this, context, uid.getPath() );

                localItem = getLocalStorage().retrieveItem( this, context, uid.getPath() );
            }
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Item " + uid.toString() + " not found in local storage." );
            }

            throw ex;
        }

        return localItem;
    }

}
