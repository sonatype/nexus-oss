/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireCaches;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreUpdate;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.ReadLockingContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.DefaultLocalStorageContext;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerException;
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
    extends ConfigurableRepository
    implements Repository
{
    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private CacheManager cacheManager;

    @Requirement
    private TargetRegistry targetRegistry;

    @Requirement
    private RepositoryItemUidFactory repositoryItemUidFactory;

    @Requirement
    private RepositoryItemUidAttributeManager repositoryItemUidAttributeManager;

    @Requirement
    private AccessManager accessManager;

    @Requirement
    private Walker walker;

    @Requirement
    private MimeUtil mimeUtil;

    @Requirement
    private MimeSupport mimeSupport;

    @Requirement( role = ContentGenerator.class )
    private Map<String, ContentGenerator> contentGenerators;

    @Requirement
    private AttributesHandler attributesHandler;

    /** Local storage context to store storage-wide configs. */
    private LocalStorageContext localStorageContext;

    /** The local storage. */
    private LocalRepositoryStorage localStorage;

    /** The not found cache. */
    private PathCache notFoundCache;

    /** Request processors list */
    private Map<String, RequestProcessor> requestProcessors;

    /** if local url changed, need special handling after save */
    private boolean localUrlChanged = false;

    /** if non-indexable -> indexable change occured, need special handling after save */
    private boolean madeSearchable = false;

    /** if local status changed, need special handling after save */
    private boolean localStatusChanged = false;


    // --

    protected Logger getLogger()
    {
        return logger;
    }

    @Deprecated
    protected MimeUtil getMimeUtil()
    {
        return mimeUtil;
    }

    protected MimeSupport getMimeSupport()
    {
        return mimeSupport;
    }

    @Override
    public MimeRulesSource getMimeRulesSource()
    {
        return MimeRulesSource.NOOP;
    }

    // ==

    @Override
    protected abstract Configurator getConfigurator();

    @Override
    protected abstract CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory();

    @Override
    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean wasDirty = super.commitChanges();

        if ( wasDirty )
        {
            getApplicationEventMulticaster().notifyEventListeners( getRepositoryConfigurationUpdatedEvent() );
        }

        this.localUrlChanged = false;

        this.madeSearchable = false;

        this.localStatusChanged = false;

        return wasDirty;
    }

    @Override
    public boolean rollbackChanges()
    {
        this.localUrlChanged = false;

        this.madeSearchable = false;

        this.localStatusChanged = false;

        return super.rollbackChanges();
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    protected RepositoryConfigurationUpdatedEvent getRepositoryConfigurationUpdatedEvent()
    {
        RepositoryConfigurationUpdatedEvent event = new RepositoryConfigurationUpdatedEvent( this );

        event.setLocalUrlChanged( this.localUrlChanged );
        event.setMadeSearchable( this.madeSearchable );
        event.setLocalStatusChanged( localStatusChanged );

        return event;
    }

    protected AbstractRepositoryConfiguration getExternalConfiguration( boolean forModification )
    {
        return (AbstractRepositoryConfiguration) getCurrentCoreConfiguration().getExternalConfiguration().getConfiguration(
            forModification );
    }

    // ==

    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        // we are allowing all, and subclasses will filter as they want
        return new DefaultRepositoryTaskFilter().setAllowsRepositoryScanning( true ).setAllowsScheduledTasks( true ).setAllowsUserInitiatedTasks(
            true ).setContentOperators( DefaultRepositoryTaskActivityDescriptor.ALL_CONTENT_OPERATIONS ).setAttributeOperators(
            DefaultRepositoryTaskActivityDescriptor.ALL_ATTRIBUTES_OPERATIONS );
    }

    public Map<String, RequestProcessor> getRequestProcessors()
    {
        if ( requestProcessors == null )
        {
            requestProcessors = new HashMap<String, RequestProcessor>();
        }

        return requestProcessors;
    }

    /**
     * Gets the cache manager.
     * 
     * @return the cache manager
     */
    protected CacheManager getCacheManager()
    {
        return cacheManager;
    }

    /**
     * Sets the cache manager.
     * 
     * @param cacheManager the new cache manager
     */
    protected void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    /**
     * Returns the repository Item Uid Factory.
     * 
     * @return
     */
    protected RepositoryItemUidFactory getRepositoryItemUidFactory()
    {
        return repositoryItemUidFactory;
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

    @Override
    public void setIndexable( boolean indexable )
    {
        if ( !isIndexable() && indexable )
        {
            // we have a non-indexable -> indexable transition
            madeSearchable = true;
        }

        super.setIndexable( indexable );
    }

    @Override
    public void setLocalUrl( String localUrl )
        throws StorageException
    {
        String newLocalUrl = null;

        if ( localUrl != null )
        {
            newLocalUrl = localUrl.trim();
        }

        if ( newLocalUrl != null )
        {
            if ( newLocalUrl.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
            {
                newLocalUrl = newLocalUrl.substring( 0, newLocalUrl.length() - 1 );
            }

            getLocalStorage().validateStorageUrl( newLocalUrl );
        }

        // Dont use getLocalUrl since that applies default
        if ( getCurrentConfiguration( false ).getLocalStorage() != null
            && !StringUtils.equals( newLocalUrl, getCurrentConfiguration( false ).getLocalStorage().getUrl() ) )
        {
            this.localUrlChanged = true;
        }

        super.setLocalUrl( localUrl );
    }

    @Override
    public void setLocalStatus( LocalStatus localStatus )
    {
        if ( !localStatus.equals( getLocalStatus() ) )
        {
            LocalStatus oldLocalStatus = getLocalStatus();

            super.setLocalStatus( localStatus );

            localStatusChanged = true;

            getApplicationEventMulticaster().notifyEventListeners(
                new RepositoryEventLocalStatusChanged( this, oldLocalStatus, localStatus ) );
        }
    }

    @SuppressWarnings( "unchecked" )
    public <F> F adaptToFacet( Class<F> t )
    {
        if ( getRepositoryKind().isFacetAvailable( t ) )
        {
            return (F) this;
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

    public void expireCaches( ResourceStoreRequest request )
    {
        if ( !getLocalStatus().shouldServiceRequest() )
        {
            return;
        }

        if ( StringUtils.isEmpty( request.getRequestPath() ) )
        {
            request.setRequestPath( RepositoryItemUid.PATH_ROOT );
        }

        request.setRequestLocalOnly( true );

        getLogger().info(
            "Expiring local cache in repository ID='" + getId() + "' from path='" + request.getRequestPath() + "'" );

        // 1st, expire all the files below path
        DefaultWalkerContext ctx = new DefaultWalkerContext( this, request );

        ctx.getProcessors().add( new ExpireCacheWalker( this ) );

        try
        {
            getWalker().walk( ctx );
        }
        catch ( WalkerException e )
        {
            if ( !( e.getWalkerContext().getStopCause() instanceof ItemNotFoundException ) )
            {
                // everything that is not ItemNotFound should be reported,
                // otherwise just neglect it
                throw e;
            }
        }

        // 2nd, remove the items from NFC
        expireNotFoundCaches( request );
    }

    public void expireNotFoundCaches( ResourceStoreRequest request )
    {
        if ( !getLocalStatus().shouldServiceRequest() )
        {
            return;
        }

        if ( StringUtils.isBlank( request.getRequestPath() ) )
        {
            request.setRequestPath( RepositoryItemUid.PATH_ROOT );
        }

        getLogger().info(
            "Clearing NFC cache in repository ID='" + getId() + "' from path='" + request.getRequestPath() + "'" );

        // remove the items from NFC
        if ( RepositoryItemUid.PATH_ROOT.equals( request.getRequestPath() ) )
        {
            // purge all
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
                getNotFoundCache().removeWithParents( request.getRequestPath() );

                getNotFoundCache().removeWithChildren( request.getRequestPath() );
            }
        }

        getApplicationEventMulticaster().notifyEventListeners(
            new RepositoryEventExpireCaches( this, request.getRequestPath() ) );
    }

    public Collection<String> evictUnusedItems( ResourceStoreRequest request, final long timestamp )
    {
        // this is noop at this "level"

        return Collections.emptyList();
    }

    public boolean recreateAttributes( ResourceStoreRequest request, final Map<String, String> initialData )
    {
        if ( !getLocalStatus().shouldServiceRequest() )
        {
            return false;
        }

        if ( StringUtils.isEmpty( request.getRequestPath() ) )
        {
            request.setRequestPath( RepositoryItemUid.PATH_ROOT );
        }

        getLogger().info(
            "Rebuilding attributes in repository ID='" + getId() + "' from path='" + request.getRequestPath() + "'" );

        RecreateAttributesWalker walkerProcessor = new RecreateAttributesWalker( this, initialData );

        DefaultWalkerContext ctx = new DefaultWalkerContext( this, request );

        ctx.getProcessors().add( walkerProcessor );

        // let it loose
        try
        {
            getWalker().walk( ctx );
        }
        catch ( WalkerException e )
        {
            if ( !( e.getWalkerContext().getStopCause() instanceof ItemNotFoundException ) )
            {
                // everything that is not ItemNotFound should be reported,
                // otherwise just neglect it
                throw e;
            }
        }

        getApplicationEventMulticaster().notifyEventListeners( new RepositoryEventRecreateAttributes( this ) );

        return true;
    }

    public AttributesHandler getAttributesHandler()
    {
        return attributesHandler;
    }

    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        this.attributesHandler = attributesHandler;
    }

    public LocalStorageContext getLocalStorageContext()
    {
        if ( localStorageContext == null )
        {
            localStorageContext =
                new DefaultLocalStorageContext( getApplicationConfiguration().getGlobalLocalStorageContext() );
        }

        return localStorageContext;
    }

    public LocalRepositoryStorage getLocalStorage()
    {
        return localStorage;
    }

    public void setLocalStorage( LocalRepositoryStorage localStorage )
    {
        getCurrentConfiguration( true ).getLocalStorage().setProvider( localStorage.getProviderId() );

        this.localStorage = localStorage;
    }

    // ===================================================================================
    // Store iface

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
    {
        if ( !checkConditions( request, Action.read ) )
        {
            throw new ItemNotFoundException( request, this );
        }

        StorageItem item = retrieveItem( false, request );

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) && !isBrowseable() )
        {
            getLogger().debug(
                getId() + " retrieveItem() :: FOUND a collection on " + request.toString()
                    + " but repository is not Browseable." );

            throw new ItemNotFoundException( request, this );
        }

        return item;
    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
        StorageException, AccessDeniedException
    {
        if ( !checkConditions( from, Action.read ) )
        {
            throw new IllegalRequestException( from, "copyItem: Operation does not fills needed requirements!" );
        }
        if ( !checkConditions( to, getResultingActionOnWrite( to ) ) )
        {
            throw new IllegalRequestException( to, "copyItem: Operation does not fills needed requirements!" );
        }

        copyItem( false, from, to );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
        StorageException, AccessDeniedException
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

        moveItem( false, from, to );
    }

    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
        StorageException, AccessDeniedException
    {
        if ( !checkConditions( request, Action.delete ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        deleteItem( false, request );
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException, AccessDeniedException
    {
        if ( !checkConditions( request, getResultingActionOnWrite( request ) ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        DefaultStorageFileItem fItem =
            new DefaultStorageFileItem( this, request, true, true, new PreparedContentLocator( is,
                getMimeSupport().guessMimeTypeFromPath( getMimeRulesSource(), request.getRequestPath() ) ) );

        if ( userAttributes != null )
        {
            fItem.getRepositoryItemAttributes().putAll( userAttributes );
        }

        storeItem( false, fItem );
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException, AccessDeniedException
    {
        if ( !checkConditions( request, getResultingActionOnWrite( request ) ) )
        {
            throw new AccessDeniedException( request, "Operation does not fills needed requirements!" );
        }

        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, request, true, true );

        if ( userAttributes != null )
        {
            coll.getRepositoryItemAttributes().putAll( userAttributes );
        }

        storeItem( false, coll );
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
    {
        if ( !checkConditions( request, Action.read ) )
        {
            throw new ItemNotFoundException( request, this );
        }

        Collection<StorageItem> items = null;

        if ( isBrowseable() )
        {
            items = list( false, request );
        }
        else
        {
            throw new ItemNotFoundException( request, this );
        }

        return items;
    }

    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "getTargetsForRequest() :: " + this.getId() + ":" + request.getRequestPath() );
        }

        return targetRegistry.getTargetsForRepositoryPath( this, request.getRequestPath() );
    }

    public boolean hasAnyTargetsForRequest( ResourceStoreRequest request )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "hasAnyTargetsForRequest() :: " + this.getId() );
        }

        return targetRegistry.hasAnyApplicableTarget( this );
    }

    public Action getResultingActionOnWrite( final ResourceStoreRequest rsr )
        throws LocalStorageException
    {
        final boolean isInLocalStorage = getLocalStorage().containsItem( this, rsr );

        if ( isInLocalStorage )
        {
            return Action.update;
        }
        else
        {
            return Action.create;
        }
    }

    // ===================================================================================
    // Repositry store-like

    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".retrieveItem() :: " + request.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        request.addProcessedRepository( this );

        maintainNotFoundCache( request );

        final RepositoryItemUid uid = createUid( request.getRequestPath() );

        final RepositoryItemUidLock uidLock = uid.getLock();

        uidLock.lock( Action.read );

        try
        {
            StorageItem item = doRetrieveItem( request );

            // file with generated content?
            if ( item instanceof StorageFileItem && ( (StorageFileItem) item ).isContentGenerated() )
            {
                StorageFileItem file = (StorageFileItem) item;

                String key = file.getContentGeneratorId();

                if ( getContentGenerators().containsKey( key ) )
                {
                    ContentGenerator generator = getContentGenerators().get( key );

                    try
                    {
                        file.setContentLocator( generator.generateContent( this, uid.getPath(), file ) );
                    }
                    catch ( Exception e )
                    {
                        throw new LocalStorageException( "Could not generate content:", e );
                    }
                }
                else
                {
                    getLogger().info(
                        "The file in repository ID='" + this.getId() + "' on path='" + uid.getPath()
                            + "' should be generated by ContentGeneratorId='" + key + "', but it does not exists!" );

                    throw new ItemNotFoundException( request, this );
                }
            }
            // plain file? wrap it
            else if ( item instanceof StorageFileItem )
            {
                StorageFileItem file = (StorageFileItem) item;

                // wrap the content locator if needed
                if ( !( file.getContentLocator() instanceof ReadLockingContentLocator ) )
                {
                    file.setContentLocator( new ReadLockingContentLocator( uid, file.getContentLocator() ) );
                }
            }

            getApplicationEventMulticaster().notifyEventListeners( new RepositoryItemEventRetrieve( this, item ) );

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

            if ( shouldAddToNotFoundCache( request ) )
            {
                addToNotFoundCache( request );
            }

            throw ex;
        }
        finally
        {
            uidLock.unlock();
        }
    }

    public void copyItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".copyItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        maintainNotFoundCache( from );

        final RepositoryItemUid fromUid = createUid( from.getRequestPath() );

        final RepositoryItemUid toUid = createUid( to.getRequestPath() );

        final RepositoryItemUidLock fromUidLock = fromUid.getLock();

        final RepositoryItemUidLock toUidLock = toUid.getLock();

        fromUidLock.lock( Action.read );
        toUidLock.lock( Action.create );

        try
        {
            StorageItem item = retrieveItem( fromTask, from );

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                try
                {
                    DefaultStorageFileItem target =
                        new DefaultStorageFileItem( this, to, true, true, new PreparedContentLocator(
                            ( (StorageFileItem) item ).getInputStream(), ( (StorageFileItem) item ).getMimeType() ) );

                    target.getItemContext().putAll( item.getItemContext() );

                    storeItem( fromTask, target );

                    // remove the "to" item from n-cache if there
                    removeFromNotFoundCache( to );
                }
                catch ( IOException e )
                {
                    throw new LocalStorageException( "Could not get the content of source file (is it file?)!", e );
                }
            }
        }
        finally
        {
            toUidLock.unlock();

            fromUidLock.unlock();
        }
    }

    public void moveItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".moveItem() :: " + from.toString() + " --> " + to.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        copyItem( fromTask, from, to );

        deleteItem( fromTask, from );
    }

    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".deleteItem() :: " + request.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        maintainNotFoundCache( request );

        final RepositoryItemUid uid = createUid( request.getRequestPath() );

        final RepositoryItemUidLock uidLock = uid.getLock();

        uidLock.lock( Action.delete );

        try
        {
            // determine is the thing to be deleted a collection or not
            StorageItem item = getLocalStorage().retrieveItem( this, request );

            // fire the event for file being deleted
            getApplicationEventMulticaster().notifyEventListeners( new RepositoryItemEventDelete( this, item ) );

            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "We are deleting a collection, starting a walker to send delete notifications per-file." );
                }

                // it is collection, walk it and below and fire events for all files
                DeletionNotifierWalker dnw = new DeletionNotifierWalker( getApplicationEventMulticaster(), request );

                DefaultWalkerContext ctx = new DefaultWalkerContext( this, request );

                ctx.getProcessors().add( dnw );

                try
                {
                    getWalker().walk( ctx );
                }
                catch ( WalkerException e )
                {
                    if ( !( e.getWalkerContext().getStopCause() instanceof ItemNotFoundException ) )
                    {
                        // everything that is not ItemNotFound should be reported,
                        // otherwise just neglect it
                        throw e;
                    }
                }
            }

            doDeleteItem( request );
        }
        finally
        {
            uidLock.unlock();
        }
    }

    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".storeItem() :: " + item.getRepositoryItemUid().toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        final RepositoryItemUid uid = createUid( item.getPath() );

        // replace UID to own one
        item.setRepositoryItemUid( uid );

        // NEXUS-4550: This "fake" UID/lock here is introduced only to serialize uploaders
        // This will catch immediately an uploader if an upload already happens
        // and prevent deadlocks, since uploader still does not have
        // shared lock
        final RepositoryItemUid uploaderUid = createUid( item.getPath() + ".storeItem()" );

        final RepositoryItemUidLock uidUploaderLock = uploaderUid.getLock();

        uidUploaderLock.lock( Action.create );

        final Action action = getResultingActionOnWrite( item.getResourceStoreRequest() );

        try
        {
            // NEXUS-4550: we are shared-locking the actual UID (to not prevent downloaders while
            // we save to temporary location. But this depends on actual LS backend actually...)
            // but we exclusive lock uploaders to serialize them!
            // And the LS has to take care of whatever stricter locking it has to use or not
            // Think: RDBMS LS or some trickier LS implementations for example
            final RepositoryItemUidLock uidLock = uid.getLock();

            uidLock.lock( Action.read );

            try
            {
                // store it
                getLocalStorage().storeItem( this, item );
            }
            finally
            {
                uidLock.unlock();
            }
        }
        finally
        {
            uidUploaderLock.unlock();
        }

        // remove the "request" item from n-cache if there
        removeFromNotFoundCache( item.getResourceStoreRequest() );

        if ( Action.create.equals( action ) )
        {
            getApplicationEventMulticaster().notifyEventListeners( new RepositoryItemEventStoreCreate( this, item ) );
        }
        else
        {
            getApplicationEventMulticaster().notifyEventListeners( new RepositoryItemEventStoreUpdate( this, item ) );
        }
    }

    public Collection<StorageItem> list( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".list() :: " + request.toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        request.addProcessedRepository( this );

        StorageItem item = retrieveItem( fromTask, request );

        if ( item instanceof StorageCollectionItem )
        {
            return list( fromTask, (StorageCollectionItem) item );
        }
        else
        {
            throw new ItemNotFoundException( request, this );
        }
    }

    public Collection<StorageItem> list( boolean fromTask, StorageCollectionItem coll )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( getId() + ".list() :: " + coll.getRepositoryItemUid().toString() );
        }

        if ( !getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        maintainNotFoundCache( coll.getResourceStoreRequest() );

        Collection<StorageItem> items = doListItems( new ResourceStoreRequest( coll ) );

        for ( StorageItem item : items )
        {
            item.getItemContext().putAll( coll.getItemContext() );
        }

        return items;
    }

    @Override
    public RepositoryItemUid createUid( final String path )
    {
        return getRepositoryItemUidFactory().createUid( this, path );
    }

    public RepositoryItemUidAttributeManager getRepositoryItemUidAttributeManager()
    {
        return repositoryItemUidAttributeManager;
    }

    // ===================================================================================
    // Inner stuff
    /**
     * Maintains not found cache.
     * 
     * @param path the path
     * @throws ItemNotFoundException the item not found exception
     */
    public void maintainNotFoundCache( ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        if ( isNotFoundCacheActive() )
        {
            if ( getNotFoundCache().contains( request.getRequestPath() ) )
            {
                if ( getNotFoundCache().isExpired( request.getRequestPath() ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "The path " + request.getRequestPath() + " is in NFC but expired." );
                    }

                    removeFromNotFoundCache( request );
                }
                else
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "The path " + request.getRequestPath()
                                + " is in NFC and still active, throwing ItemNotFoundException." );
                    }

                    throw new ItemNotFoundException( request );
                }
            }
        }
    }

    @Deprecated
    public void addToNotFoundCache( String path )
    {
        addToNotFoundCache( new ResourceStoreRequest( path ) );
    }

    @Deprecated
    public void removeFromNotFoundCache( String path )
    {
        removeFromNotFoundCache( new ResourceStoreRequest( path ) );
    }

    /**
     * Adds the uid to not found cache.
     * 
     * @param path the path
     */
    @Override
    public void addToNotFoundCache( ResourceStoreRequest request )
    {
        if ( isNotFoundCacheActive() )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Adding path " + request.getRequestPath() + " to NFC." );
            }

            getNotFoundCache().put( request.getRequestPath(), Boolean.TRUE, getNotFoundCacheTimeToLive() * 60 );
        }
    }

    /**
     * Removes the uid from not found cache.
     * 
     * @param path the path
     */
    public void removeFromNotFoundCache( ResourceStoreRequest request )
    {
        if ( isNotFoundCacheActive() )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Removing path " + request.getRequestPath() + " from NFC." );
            }

            getNotFoundCache().removeWithParents( request.getRequestPath() );
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
        throws IllegalOperationException, AccessDeniedException
    {
        if ( !this.getLocalStatus().shouldServiceRequest() )
        {
            throw new RepositoryNotAvailableException( this );
        }

        // check for writing to read only repo
        // Readonly is ALWAYS read only
        if ( RepositoryWritePolicy.READ_ONLY.equals( getWritePolicy() ) && !isActionAllowedReadOnly( action ) )
        {
            throw new IllegalRequestException( request, "Repository with ID='" + getId()
                + "' is Read Only, but action was '" + action.toString() + "'!" );
        }
        // but Write/write once may need to allow updating metadata
        // check the write policy
        enforceWritePolicy( request, action );

        if ( isExposed() )
        {
            getAccessManager().decide( this, request, action );
        }

        return checkRequestProcessors( request, action );
    }

    protected boolean checkRequestProcessors( final ResourceStoreRequest request, final Action action )
    {
        if ( getRequestProcessors().size() > 0 )
        {
            for ( RequestProcessor processor : getRequestProcessors().values() )
            {
                if ( !processor.process( this, request, action ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    protected void enforceWritePolicy( ResourceStoreRequest request, Action action )
        throws IllegalRequestException
    {
        // check for write once (no redeploy)
        if ( Action.update.equals( action ) && !RepositoryWritePolicy.ALLOW_WRITE.equals( this.getWritePolicy() ) )
        {
            throw new IllegalRequestException( request, "Repository with ID='" + getId()
                + "' does not allow updating artifacts." );
        }
    }

    public boolean isCompatible( Repository repository )
    {
        return getRepositoryContentClass().isCompatible( repository.getRepositoryContentClass() );
    }

    @Deprecated
    protected AbstractStorageItem createStorageItem( ResourceStoreRequest request, byte[] bytes )
    {
        ContentLocator content =
            new ByteArrayContentLocator( bytes, getMimeUtil().getMimeType( request.getRequestPath() ) );

        DefaultStorageFileItem result =
            new DefaultStorageFileItem( this, request, true /* isReadable */, false /* isWritable */, content );
        result.setLength( bytes.length );

        return result;
    }

    protected void doDeleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, ItemNotFoundException, StorageException
    {
        getLocalStorage().deleteItem( this, request );
    }

    protected Collection<StorageItem> doListItems( ResourceStoreRequest request )
        throws ItemNotFoundException, StorageException
    {
        return getLocalStorage().listItems( this, request );
    }

    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        AbstractStorageItem localItem = null;

        try
        {
            localItem = getLocalStorage().retrieveItem( this, request );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Item " + request.toString() + " found in local storage." );
            }
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Item " + request.toString() + " not found in local storage." );
            }

            throw ex;
        }

        return localItem;
    }

    protected boolean isActionAllowedReadOnly( Action action )
    {
        return action.isReadAction();
    }

    /**
     * Whether or not the requested path should be added to NFC. Item will be added to NFC if is not local/remote only.
     * 
     * @param request resource store request
     * @return true if requested path should be added to NFC
     * @since 1.10.0
     */
    protected boolean shouldAddToNotFoundCache( final ResourceStoreRequest request )
    {
        // if not local/remote only, add it to NFC
        return !request.isRequestLocalOnly() && !request.isRequestRemoteOnly();
    }

}
