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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.InvalidItemContentException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mirror.DownloadMirrorSelector;
import org.sonatype.nexus.proxy.mirror.DownloadMirrors;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

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

    protected static final int DOWNLOAD_RETRY_COUNT = 2;

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    /**
     * Feed recorder.
     */
    @Requirement
    private FeedRecorder feedRecorder;

    /** The proxy mode */
    private volatile ProxyMode proxyMode = ProxyMode.ALLOW;

    /** The proxy remote status */
    private volatile RemoteStatus remoteStatus = RemoteStatus.UNKNOWN;

    /** The repo status check mode */
    private volatile RepositoryStatusCheckMode repositoryStatusCheckMode = RepositoryStatusCheckMode.NEVER;

    /** Last time remote status was updated */
    private volatile long remoteStatusUpdated = 0;

    /** The remote storage. */
    private RemoteRepositoryStorage remoteStorage;

    /** The remote url. */
    private String remoteUrl;

    /** Remote storage context to store connection configs. */
    private RemoteStorageContext remoteStorageContext;

    /**
     * The item max age.
     */
    private int itemMaxAge = 24 * 60;

    @Requirement
    private DownloadMirrors mirrors;

    protected void resetRemoteStatus()
    {
        remoteStatusUpdated = 0;
    }

    protected boolean isRemoteStorageReachable()
        throws StorageException,
            RemoteAuthenticationNeededException,
            RemoteAccessDeniedException
    {
        try
        {
            // TODO: include context? from where?
            return getRemoteStorage().isReachable( this, null );
        }
        catch ( RemoteAccessException ex )
        {
            getLogger().warn(
                "RemoteStorage of repository " + getId()
                    + " throws RemoteAccessException. Please set up authorization information for repository ID='"
                    + this.getId()
                    + "'. Setting ProxyMode of this repository to BlockedAuto. MANUAL INTERVENTION NEEDED.",
                ex );

            autoBlockProxying( ex );

            return false;
        }
    }

    /** Is checking in progress? */
    private volatile boolean _remoteStatusChecking = false;

    public RemoteStatus getRemoteStatus( boolean forceCheck )
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
        if ( getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
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
            notifyProximityEventListeners( new RepositoryEventProxyModeChanged( this, oldProxyMode, proxyMode, cause ) );
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
        return repositoryStatusCheckMode;
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        this.repositoryStatusCheckMode = mode;
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

    public RemoteRepositoryStorage getRemoteStorage()
    {
        return remoteStorage;
    }

    public void setRemoteStorage( RemoteRepositoryStorage remoteStorage )
    {
        this.remoteStorage = remoteStorage;

        setAllowWrite( remoteStorage == null );
    }

    public DownloadMirrors getDownloadMirrors()
    {
        return mirrors;
    }

    public void setMirrorUrls( List<String> urls )
    {
        mirrors.setUrls( urls );
    }

    protected AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws StorageException
    {
        if ( Boolean.TRUE.equals( item.getItemContext().get( CTX_TRANSITIVE_ITEM ) ) )
        {
            return item;
        }

        AbstractStorageItem result = null;

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Caching item " + item.getRepositoryItemUid().toString() + " in local storage of repository." );
            }

            getLocalStorage().storeItem( this, item.getItemContext(), item );

            removeFromNotFoundCache( item.getRepositoryItemUid().getPath() );

            result = getLocalStorage()
                .retrieveItem( this, item.getItemContext(), item.getRepositoryItemUid().getPath() );

            notifyProximityEventListeners( new RepositoryItemEventCache( this, result ) );

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

    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem item = null;
        AbstractStorageItem remoteItem = null;
        boolean localOnly = context != null && context.containsKey( ResourceStoreRequest.CTX_LOCAL_ONLY_FLAG )
            && Boolean.TRUE.equals( context.get( ResourceStoreRequest.CTX_LOCAL_ONLY_FLAG ) );

        AbstractStorageItem localItem = (AbstractStorageItem) super.doRetrieveItem( uid, context );

        boolean shouldProxy = true;

        for ( RequestProcessor processor : getRequestProcessors() )
        {
            shouldProxy = shouldProxy && processor.shouldProxy( this, uid, context );
        }

        if ( getProxyMode() != null && getProxyMode().shouldProxy() && !localOnly && shouldProxy )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "ProxyMode is " + getProxyMode().toString() );
            }

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
                                "Item " + uid.toString() + " is old, checking for newer file on remote then local: "
                                    + new Date( localItem.getModified() ) );
                        }

                        // check is the remote newer than the local one
                        try
                        {
                            shouldGetRemote = getRemoteStorage().containsItem(
                                localItem.getModified(),
                                this,
                                context,
                                uid.getPath() );

                            if ( !shouldGetRemote )
                            {
                                markItemRemotelyChecked( uid, context );
                            }

                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug(
                                    "Newer version of item " + uid.toString() + " is found on remote storage." );
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
                            remoteItem = doRetrieveRemoteItem( uid, context );

                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug( "Item " + uid.toString() + " found in remote storage." );
                            }
                        }
                        catch ( StorageException e )
                        {
                            remoteItem = null;

                            // cleanup if any remnant is here
                            try
                            {
                                deleteItem( uid, context );
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
                        getLogger().debug( "Item " + uid.toString() + " not found in remote storage." );
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

    private void sendContentValidationEvents( RepositoryItemUid uid, List<NexusArtifactEvent> events, boolean isContentValid )
    {
        if ( getLogger().isDebugEnabled() && !isContentValid )
        {
            getLogger().debug(
                "Item " + uid.toString() + " failed content integrity validation." );
        }

        for ( NexusArtifactEvent event : events )
        {
            feedRecorder.addNexusArtifactEvent( event );
        }
    }

    protected void markItemRemotelyChecked( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException,
            ItemNotFoundException
    {
        // remote file unchanged, touch the local one to renew it's Age
        getLocalStorage().touchItemRemoteChecked( this, context, uid.getPath() );
    }

    /**
     * Validates integrity of content of <code>item</code>. Retruns <code>true</code>
     * if item content is valid and <code>false</code> if item content is corrupted.
     * 
     * Note that this method is called doRetrieveRemoteItem, so implementation
     * must retrieve checksum files directly from remote storage 
     * 
     * <code>
     *   getRemoteStorage().retrieveItem( this, context, getRemoteUrl(), checksumUid.getPath() );
     * </code>
     */
    protected boolean doValidateRemoteItemContent( String baseUrl, AbstractStorageItem item, Map<String, Object> context, List<NexusArtifactEvent> events )
        throws StorageException
    {
        return true;
    }

    /**
     * Retrieves item with specified uid from remote storage according to the
     * following retry-fallback-blacklist rules.
     *
     * <li>
     * Only retrieve item operation will use mirrors, other operations,
     *   like check availability and retrieve checksum file, will always
     *   use repository canonical url.
     *   </li>
     * <li>
     * Only one mirror url will be considered before retrieve item operation
     *   falls back to repository canonical url.
     *   </li>
     * <li>
     * Repository canonical url will never be put on the blacklist.
     *   </li>
     * <li>
     * If retrieve item operation fails with ItemNotFound or AccessDenied
     *   error, the operation will be retried with another url or original
     *   error will be reported if there are no more urls.
     *   </li>
     * <li>  
     * If retrieve item operation fails with generic StorageException or
     *   item content is corrupt, the operation will be retried one more
     *   time from the same url. After that, the operation will be retried
     *   with another url or original error will be returned if there are
     *   no more urls.
     *   </li>
     * <li>  
     * Mirror url will be put on the blacklist if retrieve item operation
     *   from the url failed with StorageException, AccessDenied or InvalidItemContent error
     *   but the item was successfully retrieve from another url.
     *   </li>
     * <li>
     * Mirror url will be removed from blacklist after 30 minutes.
     *   </li>
     * 
     * 
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
     * 
     * 
     * 
     */
    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            RemoteAccessException,
            StorageException
    {
        DownloadMirrorSelector selector = getDownloadMirrors().openSelector();

        ArrayList<String> urls = new ArrayList<String>( selector.getUrls() );
        urls.add( getRemoteUrl() );

        ArrayList<NexusArtifactEvent> events = new ArrayList<NexusArtifactEvent>();

        Exception lastException = null;

        try
        {
            all_urls:
            for ( String baseUrl : urls )
            {
                for ( int i = 0; i < DOWNLOAD_RETRY_COUNT; i++ )
                {
                    try
                    {
                        // events.clear();
    
                        AbstractStorageItem remoteItem = getRemoteStorage().retrieveItem( this, context, baseUrl, uid.getPath() );
    
                        remoteItem.getItemContext().putAll( context );
    
                        remoteItem = doCacheItem( remoteItem );
    
                        if ( doValidateRemoteItemContent( baseUrl, remoteItem, context, events ) )
                        {
                            sendContentValidationEvents( uid, events, true );

                            selector.feedbackSuccess( baseUrl );

                            return remoteItem; 
                        }
                        else
                        {
                            selector.feedbackFailure( baseUrl );

                            lastException = new InvalidItemContentException( uid );
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
    
                        selector.feedbackFailure( baseUrl );
    
                        continue all_urls; // retry with next url
                    }
                    catch ( StorageException e )
                    {
                        lastException = e;
    
                        selector.feedbackFailure( baseUrl );
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

        sendContentValidationEvents( uid, events, false );

        try
        {
            getLocalStorage().deleteItem( this, context, uid.getPath() );
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
        throw new ItemNotFoundException( uid );

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

        // a directory is not "aged"
        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            return false;
        }

        // if repo is non-expirable, false
        if ( maxAge < 1 )
        {
            return false;
        }
        // else check age
        else
        {
            return ( ( System.currentTimeMillis() - item.getRemoteChecked() ) > ( maxAge * 60L * 1000L ) );
        }
    }

}
