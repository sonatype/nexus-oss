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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.InvalidItemContentException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mirror.DefaultDownloadMirrors;
import org.sonatype.nexus.proxy.mirror.DownloadMirrorSelector;
import org.sonatype.nexus.proxy.mirror.DownloadMirrors;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalkerProcessor.EvictUnusedItemsWalkerFilter;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

/**
 * Adds the proxying capability to a simple repository. The proxying will happen only if reposiory has remote storage!
 * So, this implementation is used in both "simple" repository cases: hosted and proxy, but in 1st case there is no
 * remote storage.
 * 
 * @author cstamas
 */
public abstract class AbstractProxyRepository
    extends AbstractRepository
    implements ProxyRepository
{
    /** The time while we do NOT check an already known remote status: 5 mins */
    protected static final long REMOTE_STATUS_RETAIN_TIME = 5L * 60L * 1000L;

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    /** if remote url changed, need special handling after save */
    private boolean remoteUrlChanged = false;

    /** The proxy remote status */
    private volatile RemoteStatus remoteStatus = RemoteStatus.UNKNOWN;

    /** Last time remote status was updated */
    private volatile long remoteStatusUpdated = 0;

    /** The remote storage. */
    private RemoteRepositoryStorage remoteStorage;

    /** Remote storage context to store connection configs. */
    private RemoteStorageContext remoteStorageContext;

    /** Proxy selector, if set */
    private ProxySelector proxySelector;

    /** Download mirrors */
    private DownloadMirrors dMirrors;

    /** Item content validators */
    private Map<String, ItemContentValidator> itemContentValidators;

    @Override
    protected AbstractProxyRepositoryConfiguration getExternalConfiguration( boolean forModification )
    {
        return (AbstractProxyRepositoryConfiguration) getCurrentCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forModification );
    }

    @Override
    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean result = super.commitChanges();

        if ( result )
        {
            this.remoteUrlChanged = false;
        }

        return result;
    }

    @Override
    public boolean rollbackChanges()
    {
        this.remoteUrlChanged = false;

        return super.rollbackChanges();
    }

    @Override
    protected RepositoryConfigurationUpdatedEvent getRepositoryConfigurationUpdatedEvent()
    {
        RepositoryConfigurationUpdatedEvent event = super.getRepositoryConfigurationUpdatedEvent();

        event.setRemoteUrlChanged( this.remoteUrlChanged );

        return event;
    }

    @Override
    public Collection<String> evictUnusedItems( ResourceStoreRequest request, final long timestamp )
    {
        if ( getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            Collection<String> result =
                doEvictUnusedItems( request, timestamp, new EvictUnusedItemsWalkerProcessor( timestamp ),
                    new EvictUnusedItemsWalkerFilter() );

            getApplicationEventMulticaster().notifyEventListeners( new RepositoryEventEvictUnusedItems( this ) );

            return result;
        }
        else
        {
            return super.evictUnusedItems( request, timestamp );
        }
    }

    protected Collection<String> doEvictUnusedItems( ResourceStoreRequest request, final long timestamp,
        EvictUnusedItemsWalkerProcessor processor, WalkerFilter filter )
    {
        getLogger().info(
            "Evicting unused items from proxy repository \"" + getName() + "\" (id=\"" + getId() + "\") from path "
                + request.getRequestPath() );

        request.setRequestLocalOnly( true );

        DefaultWalkerContext ctx = new DefaultWalkerContext( this, request, filter );

        ctx.getProcessors().add( processor );

        // and let it loose
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

        return processor.getFiles();
    }

    public Map<String, ItemContentValidator> getItemContentValidators()
    {
        if ( itemContentValidators == null )
        {
            itemContentValidators = new HashMap<String, ItemContentValidator>();
        }

        return itemContentValidators;
    }

    public boolean isItemAgingActive()
    {
        return getExternalConfiguration( false ).isItemAgingActive();
    }

    public void setItemAgingActive( boolean value )
    {
        getExternalConfiguration( true ).setItemAgingActive( value );
    }

    public ProxyMode getProxyMode()
    {
        if ( getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return getExternalConfiguration( false ).getProxyMode();
        }
        else
        {
            return null;
        }
    }

    protected void setProxyMode( ProxyMode proxyMode, boolean sendNotification, Throwable cause )
    {
        if ( getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            ProxyMode oldProxyMode = getProxyMode();

            getExternalConfiguration( true ).setProxyMode( proxyMode );

            // if this is proxy
            // and was !shouldProxy() and the new is shouldProxy()
            if ( proxyMode != null && proxyMode.shouldProxy() && !oldProxyMode.shouldProxy() )
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
                getApplicationEventMulticaster().notifyEventListeners(
                    new RepositoryEventProxyModeChanged( this, oldProxyMode, proxyMode, cause ) );
            }
        }
    }

    public void setProxyMode( ProxyMode proxyMode )
    {
        setProxyMode( proxyMode, true, null );
    }

    protected void autoBlockProxying( Throwable cause )
    {
        setRemoteStatus( RemoteStatus.UNAVAILABLE, cause );
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        return getExternalConfiguration( false ).getRepositoryStatusCheckMode();
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        getExternalConfiguration( true ).setRepositoryStatusCheckMode( mode );
    }

    public String getRemoteUrl()
    {
        if ( getCurrentConfiguration( false ).getRemoteStorage() != null )
        {
            return getCurrentConfiguration( false ).getRemoteStorage().getUrl();
        }
        else
        {
            return null;
        }
    }

    public void setRemoteUrl( String remoteUrl )
        throws StorageException
    {
        if ( getRemoteStorage() != null )
        {
            String newRemoteUrl = remoteUrl.trim();

            String oldRemoteUrl = getRemoteUrl();

            if ( newRemoteUrl.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
            {
                newRemoteUrl = newRemoteUrl.substring( 0, newRemoteUrl.length() - 1 );
            }

            getRemoteStorage().validateStorageUrl( newRemoteUrl );

            getCurrentConfiguration( true ).getRemoteStorage().setUrl( newRemoteUrl );

            if ( ( StringUtils.isEmpty( oldRemoteUrl ) && StringUtils.isNotEmpty( newRemoteUrl ) )
                || ( StringUtils.isNotEmpty( oldRemoteUrl ) && !oldRemoteUrl.equals( newRemoteUrl ) ) )
            {
                this.remoteUrlChanged = true;
            }
        }
        else
        {
            throw new StorageException( "No remote storage set on repository \"" + getName() + "\" (ID=\"" + getId()
                + "\"), cannot set remoteUrl!" );
        }
    }

    /**
     * Gets the item max age in (in minutes).
     * 
     * @return the item max age in (in minutes)
     */
    public int getItemMaxAge()
    {
        return getExternalConfiguration( false ).getItemMaxAge();
    }

    /**
     * Sets the item max age in (in minutes).
     * 
     * @param itemMaxAgeInSeconds the new item max age in (in minutes).
     */
    public void setItemMaxAge( int itemMaxAge )
    {
        getExternalConfiguration( true ).setItemMaxAge( itemMaxAge );
    }

    protected void resetRemoteStatus()
    {
        remoteStatusUpdated = 0;
    }

    protected boolean isRemoteStorageReachable( ResourceStoreRequest request )
        throws StorageException, RemoteAuthenticationNeededException, RemoteAccessDeniedException
    {
        try
        {
            return getRemoteStorage().isReachable( this, request );
        }
        catch ( RemoteAccessException ex )
        {
            getLogger().warn(
                "RemoteStorage of repository " + getId()
                    + " throws RemoteAccessException. Please set up authorization information for repository ID='"
                    + this.getId()
                    + "'. Setting ProxyMode of this repository to BlockedAuto. MANUAL INTERVENTION NEEDED.", ex );

            autoBlockProxying( ex );

            return false;
        }
    }

    /** Is checking in progress? */
    private volatile boolean _remoteStatusChecking = false;

    public RemoteStatus getRemoteStatus( ResourceStoreRequest request, boolean forceCheck )
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

            exec.submit( new RemoteStatusUpdateCallable( request ) );
        }
        else if ( getProxyMode() != null && !getProxyMode().shouldCheckRemoteStatus()
            && RemoteStatus.UNKNOWN.equals( remoteStatus ) && !_remoteStatusChecking )
        {
            setRemoteStatus( RemoteStatus.UNAVAILABLE, null );

            _remoteStatusChecking = false;
        }

        return remoteStatus;
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
        else if ( RemoteStatus.UNAVAILABLE.equals( remoteStatus ) )
        {
            this.remoteStatusUpdated = System.currentTimeMillis();

            // TODO: To enable auto-block feature, uncomment this
            // also take care that REMOTE_STATUS_RETAIN_TIME is a parameter, not
            // constant, so user can tune how long should he keep repository autoblocked.
            // if ( getProxyMode() != null && getProxyMode().shouldProxy() )
            // {
            // setProxyMode( ProxyMode.BLOCKED_AUTO, true, cause );
            // }
        }
    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        if ( remoteStorageContext == null )
        {
            remoteStorageContext =
                new DefaultRemoteStorageContext( getApplicationConfiguration().getGlobalRemoteStorageContext() );
        }

        return remoteStorageContext;
    }

    public RemoteConnectionSettings getRemoteConnectionSettings()
    {
        return getRemoteStorageContext().getRemoteConnectionSettings();
    }

    public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
    {
        getRemoteStorageContext().setRemoteConnectionSettings( settings );
    }

    public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
    {
        return getRemoteStorageContext().getRemoteAuthenticationSettings();
    }

    public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
    {
        getRemoteStorageContext().setRemoteAuthenticationSettings( settings );

        if ( getProxyMode() != null && getProxyMode().shouldAutoUnblock() )
        {
            // perm changes? retry if autoBlocked
            setProxyMode( ProxyMode.ALLOW );
        }
    }

    public RemoteProxySettings getRemoteProxySettings()
    {
        return getRemoteStorageContext().getRemoteProxySettings();
    }

    public void setRemoteProxySettings( RemoteProxySettings settings )
    {
        getRemoteStorageContext().setRemoteProxySettings( settings );

        if ( getProxyMode() != null && getProxyMode().shouldAutoUnblock() )
        {
            // perm changes? retry if autoBlocked
            setProxyMode( ProxyMode.ALLOW );
        }
    }

    public ProxySelector getProxySelector()
    {
        if ( proxySelector == null )
        {
            proxySelector = new DefaultProxySelector();
        }

        return proxySelector;
    }

    public void setProxySelector( ProxySelector selector )
    {
        this.proxySelector = selector;
    }

    public RemoteRepositoryStorage getRemoteStorage()
    {
        return remoteStorage;
    }

    public void setRemoteStorage( RemoteRepositoryStorage remoteStorage )
    {
        this.remoteStorage = remoteStorage;

        if ( remoteStorage == null )
        {
            getCurrentConfiguration( true ).setRemoteStorage( null );
        }
        else
        {
            if ( getCurrentConfiguration( true ).getRemoteStorage() == null )
            {
                getCurrentConfiguration( true ).setRemoteStorage( new CRemoteStorage() );
            }

            getCurrentConfiguration( true ).getRemoteStorage().setProvider( remoteStorage.getProviderId() );

            setWritePolicy( RepositoryWritePolicy.READ_ONLY );
        }
    }

    public DownloadMirrors getDownloadMirrors()
    {
        if ( dMirrors == null )
        {
            dMirrors = new DefaultDownloadMirrors( (CRepositoryCoreConfiguration) getCurrentCoreConfiguration() );
        }

        return dMirrors;
    }

    public AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws StorageException
    {
        // transitive items are not cached
        if ( Boolean.TRUE.equals( item.getItemContext().get( CTX_TRANSITIVE_ITEM ) ) )
        {
            return item;
        }

        boolean shouldCache = true;

        // ask request processors too
        for ( RequestProcessor processor : getRequestProcessors().values() )
        {
            shouldCache = processor.shouldCache( this, item );

            if ( !shouldCache )
            {
                return item;
            }
        }

        AbstractStorageItem result = null;

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Caching item " + item.getRepositoryItemUid().toString() + " in local storage of repository." );
            }

            item.getRepositoryItemUid().lock( getResultingActionOnWrite( new ResourceStoreRequest( item ) ) );

            try
            {
                getLocalStorage().storeItem( this, item );

                removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );

                result = getLocalStorage().retrieveItem( this, new ResourceStoreRequest( item ) );

            }
            finally
            {
                item.getRepositoryItemUid().unlock();
            }

            getApplicationEventMulticaster().notifyEventListeners( new RepositoryItemEventCache( this, result ) );

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

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            StringBuffer db = new StringBuffer( request.toString() );

            db.append( " :: localOnly=" ).append( request.isRequestLocalOnly() );
            db.append( ", remoteOnly=" ).append( request.isRequestRemoteOnly() );

            if ( getProxyMode() != null )
            {
                db.append( ", ProxyMode=" + getProxyMode().toString() );
            }

            getLogger().debug( db.toString() );
        }

        // From now on, the retrieve operation becomes WRITE operation,
        // since this is proxy repository, and it _might_ have "fetch" as side-effect, or should wait for another thread
        // to finish the fetch side effect.
        // So, for same UIDs we start serialize the request processing.
        RepositoryItemUid uid = createUid( request.getRequestPath() );

        uid.lock( Action.create );

        try
        {
            return doRetrieveItem0( request );
        }
        finally
        {
            uid.unlock();
        }
    }

    protected StorageItem doRetrieveItem0( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        AbstractStorageItem item = null;
        AbstractStorageItem remoteItem = null;
        AbstractStorageItem localItem = null;

        if ( !request.isRequestRemoteOnly() )
        {
            try
            {
                localItem = (AbstractStorageItem) super.doRetrieveItem( request );
            }
            catch ( ItemNotFoundException e )
            {
                localItem = null;
            }
        }

        // proxyMode and request.localOnly decides 1st
        boolean shouldProxy = !request.isRequestLocalOnly() && getProxyMode() != null && getProxyMode().shouldProxy();

        if ( shouldProxy )
        {
            // let's ask RequestProcessor
            for ( RequestProcessor processor : getRequestProcessors().values() )
            {
                shouldProxy = processor.shouldProxy( this, request );

                if ( !shouldProxy )
                {
                    // escape
                    break;
                }
            }
        }

        if ( shouldProxy )
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
                            getLogger().debug(
                                "Item " + request.toString()
                                    + " is old, checking for newer file on remote then local: "
                                    + new Date( localItem.getModified() ) );
                        }

                        // check is the remote newer than the local one
                        try
                        {
                            shouldGetRemote = getRemoteStorage().containsItem( localItem.getModified(), this, request );

                            if ( !shouldGetRemote )
                            {
                                markItemRemotelyChecked( request );

                                if ( getLogger().isDebugEnabled() )
                                {
                                    getLogger().debug(
                                        "No newer version of item " + request.toString() + " found on remote storage." );
                                }
                            }
                            else
                            {
                                if ( getLogger().isDebugEnabled() )
                                {
                                    getLogger().debug(
                                        "Newer version of item " + request.toString() + " is found on remote storage." );
                                }
                            }

                        }
                        catch ( RemoteAccessException ex )
                        {
                            getLogger()
                                .warn(
                                    "RemoteStorage of repository "
                                        + getId()
                                        + " throws RemoteAccessException. Please set up authorization information for repository ID='"
                                        + this.getId()
                                        + "'. Setting ProxyMode of this repository to BlockedAuto. MANUAL INTERVENTION NEEDED.",
                                    ex );

                            autoBlockProxying( ex );

                            // do not go remote, but we did not mark it as "remote checked" also.
                            // let the user do proper setup and probably it will try again
                            shouldGetRemote = false;
                        }
                        catch ( StorageException ex )
                        {
                            getLogger()
                                .warn(
                                    "RemoteStorage of repository "
                                        + getId()
                                        + " throws StorageException. Problem connecting to remote repository='"
                                        + this.getId()
                                        + "'. Setting ProxyMode of this repository to BlockedAuto. MANUAL INTERVENTION NEEDED.",
                                    ex );

                            autoBlockProxying( ex );

                            // do not go remote, but we did not mark it as "remote checked" also.
                            // let the user do proper setup and probably it will try again
                            shouldGetRemote = false;
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
                        try
                        {
                            remoteItem = doRetrieveRemoteItem( request );

                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug( "Item " + request.toString() + " found in remote storage." );
                            }
                        }
                        catch ( StorageException e )
                        {
                            remoteItem = null;

                            // cleanup if any remnant is here
                            try
                            {
                                if ( localItem == null )
                                {
                                    deleteItem( false, request );
                                }
                            }
                            catch ( ItemNotFoundException ex1 )
                            {
                                // ignore
                            }
                            catch ( UnsupportedStorageOperationException ex2 )
                            {
                                // will not happen
                            }
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
                        getLogger().debug( "Item " + request.toString() + " not found in remote storage." );
                    }

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
                                + request.toString()
                                + " does not exist in local storage neither in remote storage, throwing ItemNotFoundException." );
                }

                throw new ItemNotFoundException( request, this );
            }
            else if ( localItem != null && remoteItem == null )
            {
                // simple: we have local but not remote (coz we are offline or coz it is not newer)
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Item " + request.toString()
                            + " does exist in local storage and is fresh, returning local one." );
                }

                item = localItem;
            }
            else
            {
                // the fact that remoteItem != null means we _have_ to return that one
                // OR: we had no local copy
                // OR: remoteItem is for sure newer (look above)
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
                    getLogger()
                        .debug(
                            "Item " + request.toString()
                                + " does exist locally and cannot go remote, returning local one." );
                }

                item = localItem;
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        "Item " + request.toString()
                            + " does not exist locally and cannot go remote, throwing ItemNotFoundException." );
                }

                throw new ItemNotFoundException( request, this );
            }
        }

        return item;
    }

    private void sendContentValidationEvents( ResourceStoreRequest request, List<NexusArtifactEvent> events,
        boolean isContentValid )
    {
        if ( getLogger().isDebugEnabled() && !isContentValid )
        {
            getLogger().debug( "Item " + request.toString() + " failed content integrity validation." );
        }

        for ( NexusArtifactEvent event : events )
        {
            getFeedRecorder().addNexusArtifactEvent( event );
        }
    }

    protected void markItemRemotelyChecked( ResourceStoreRequest request )
        throws StorageException, ItemNotFoundException
    {
        // remote file unchanged, touch the local one to renew it's Age
        getLocalStorage().touchItemRemoteChecked( this, request );
    }

    /**
     * Validates integrity of content of <code>item</code>. Retruns <code>true</code> if item content is valid and
     * <code>false</code> if item content is corrupted. Note that this method is called doRetrieveRemoteItem, so
     * implementation must retrieve checksum files directly from remote storage <code>
     *   getRemoteStorage().retrieveItem( this, context, getRemoteUrl(), checksumUid.getPath() );
     * </code>
     */
    protected boolean doValidateRemoteItemContent( ResourceStoreRequest req, String baseUrl, AbstractStorageItem item,
        List<NexusArtifactEvent> events )
        throws StorageException
    {
        boolean isValid = true;

        for ( ItemContentValidator icv : getItemContentValidators().values() )
        {
            isValid = isValid && icv.isRemoteItemContentValid( this, req, baseUrl, item, events );
        }

        return isValid;
    }

    /**
     * Retrieves item with specified uid from remote storage according to the following retry-fallback-blacklist rules.
     * <li>Only retrieve item operation will use mirrors, other operations, like check availability and retrieve
     * checksum file, will always use repository canonical url.</li> <li>Only one mirror url will be considered before
     * retrieve item operation falls back to repository canonical url.</li> <li>Repository canonical url will never be
     * put on the blacklist.</li> <li>If retrieve item operation fails with ItemNotFound or AccessDenied error, the
     * operation will be retried with another url or original error will be reported if there are no more urls.</li> <li>
     * If retrieve item operation fails with generic StorageException or item content is corrupt, the operation will be
     * retried one more time from the same url. After that, the operation will be retried with another url or original
     * error will be returned if there are no more urls.</li> <li>Mirror url will be put on the blacklist if retrieve
     * item operation from the url failed with StorageException, AccessDenied or InvalidItemContent error but the item
     * was successfully retrieve from another url.</li> <li>Mirror url will be removed from blacklist after 30 minutes.</li>
     * The following matrix summarises retry/blacklist behaviour
     * 
     * <pre>
     * Error condition      Retry?        Blacklist?
     * 
     * InetNotFound         no            no
     * AccessDedied         no            yes
     * InvalidContent       yes           yes
     * Other                yes           yes
     * </pre>
     */
    protected AbstractStorageItem doRetrieveRemoteItem( ResourceStoreRequest request )
        throws ItemNotFoundException, RemoteAccessException, StorageException
    {
        DownloadMirrorSelector selector = getDownloadMirrors().openSelector();

        ArrayList<Mirror> mirrors = new ArrayList<Mirror>( selector.getMirrors() );

        mirrors.add( new Mirror( "default", getRemoteUrl() ) );

        ArrayList<NexusArtifactEvent> events = new ArrayList<NexusArtifactEvent>();

        Exception lastException = null;

        try
        {
            all_urls: for ( Mirror mirror : mirrors )
            {
                int retryCount = 1;

                if ( getRemoteStorageContext() != null )
                {
                    retryCount = getRemoteStorageContext().getRemoteConnectionSettings().getRetrievalRetryCount();
                }

                for ( int i = 0; i < retryCount; i++ )
                {
                    try
                    {
                        // events.clear();

                        AbstractStorageItem remoteItem =
                            getRemoteStorage().retrieveItem( this, request, mirror.getUrl() );

                        remoteItem.getItemContext().putAll( request.getRequestContext() );

                        remoteItem = doCacheItem( remoteItem );

                        if ( doValidateRemoteItemContent( request, mirror.getUrl(), remoteItem, events ) )
                        {
                            sendContentValidationEvents( request, events, true );

                            selector.feedbackSuccess( mirror );

                            return remoteItem;
                        }
                        else
                        {
                            selector.feedbackFailure( mirror );

                            lastException = new InvalidItemContentException( request, mirror );
                        }
                    }
                    catch ( ItemNotFoundException e )
                    {
                        lastException = e;

                        continue all_urls; // retry with next url
                    }
                    catch ( RemoteAccessException e )
                    {
                        lastException = e;

                        selector.feedbackFailure( mirror );

                        continue all_urls; // retry with next url
                    }
                    catch ( StorageException e )
                    {
                        getLogger().error(
                            "Got Storage Exception while storing remote artifact, will attempt next mirror", e );
                        lastException = e;

                        selector.feedbackFailure( mirror );
                    }

                    // retry with same url
                }
            }
        }
        finally
        {
            selector.close();
        }

        // if we got here, requested item was not retrieved for some reason

        sendContentValidationEvents( request, events, false );

        try
        {
            getLocalStorage().deleteItem( this, request );
        }
        catch ( ItemNotFoundException e )
        {
            // good, we want this item deleted
        }
        catch ( UnsupportedStorageOperationException e )
        {
            getLogger().warn( "Unexpected Exception", e );
        }

        if ( lastException instanceof StorageException )
        {
            throw (StorageException) lastException;
        }
        else if ( lastException instanceof ItemNotFoundException )
        {
            throw (ItemNotFoundException) lastException;
        }

        // validation failed, I guess.
        throw new ItemNotFoundException( request, this );

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
        return isOld( maxAge, item, isItemAgingActive() );
    }

    protected boolean isOld( int maxAge, StorageItem item, boolean shouldCalculate )
    {
        if ( !shouldCalculate )
        {
            // simply say "is old" always
            return true;
        }

        // if item is manually expired, true
        if ( item.isExpired() )
        {
            return true;
        }

        // a directory is not "aged"
        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            return false;
        }

        // if repo is non-expirable, false
        if ( maxAge < 0 )
        {
            return false;
        }
        // else check age
        else
        {
            return ( ( System.currentTimeMillis() - item.getRemoteChecked() ) > ( maxAge * 60L * 1000L ) );
        }
    }

    private class RemoteStatusUpdateCallable
        implements Callable<Object>
    {
        private ResourceStoreRequest request;

        public RemoteStatusUpdateCallable( ResourceStoreRequest request )
        {
            this.request = request;
        }

        public Object call()
            throws Exception
        {
            try
            {
                try
                {
                    if ( isRemoteStorageReachable( request ) )
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
    }

    // Need to allow delete for proxy repos
    @Override
    protected boolean isActionAllowedReadOnly( Action action )
    {
        return action.equals( Action.read ) || action.equals( Action.delete );
    }

}
