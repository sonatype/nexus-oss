/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.utils;

import java.util.Collection;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class StoreWalker.
 * 
 * @author cstamas
 * @deprecated use Walker service in org.sonatype.nexus.proxy.walker package
 */
public abstract class StoreWalker
{
    private ResourceStore store;

    private Logger logger;

    private boolean running;

    private Throwable stopCause;

    private StoreWalkerFilter filter;

    public StoreWalker( ResourceStore store, Logger logger )
    {
        this( store, logger, new AffirmativeStoreWalkerFilter() );
    }

    public StoreWalker( ResourceStore store, Logger logger, StoreWalkerFilter filter )
    {
        super();

        this.store = store;

        this.logger = logger;

        this.stopCause = null;

        this.filter = filter;
    }

    public StoreWalkerFilter getFilter()
    {
        return filter;
    }

    public void setFilter( StoreWalkerFilter filter )
    {
        this.filter = filter;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    public ResourceStore getResourceStore()
    {
        return store;
    }

    public Throwable getStopCause()
    {
        return stopCause;
    }

    public void stop()
    {
        stop( null );
    }

    public void stop( Throwable cause )
    {
        running = false;

        this.stopCause = cause;

        if ( cause != null )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Walking STOPPED on " + store.getId() + " because stop() was called with cause:", cause );
            }
        }
        else
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Walking STOPPED on " + store.getId()
                    + " because stop() was called without submitted cause." );
            }
        }
    }

    public boolean isStopped()
    {
        return !running;
    }

    public void walk()
        throws WalkerException
    {
        walk( null );
    }

    public void walk( String fromPath )
        throws WalkerException
    {
        walk( fromPath, true, false );
    }

    public final void walk( boolean localOnly, boolean collectionsOnly )
        throws WalkerException
    {
        walk( null, localOnly, collectionsOnly );
    }

    public final void walk( String fromPath, boolean localOnly, boolean collectionsOnly )
        throws WalkerException
    {
        running = true;

        if ( fromPath == null )
        {
            fromPath = RepositoryItemUid.PATH_ROOT;
        }

        if ( logger != null && logger.isDebugEnabled() )
        {
            logger.debug( "Start walking on ResourceStore " + store.getId() + " from path " + fromPath );
        }

        // user may call stop()
        beforeWalk();

        if ( !running )
        {
            return;
        }

        StorageItem item = null;

        try
        {
            if ( Repository.class.isAssignableFrom( store.getClass() ) )
            {
                // we are dealing with repository
                // this way we avoid security context processing!!!
                // TODO: enable somehow ability to pass-over the req context!
                RepositoryItemUid uid = ( (Repository) store ).createUid( fromPath );

                item = ( (Repository) store ).retrieveItem( localOnly, uid, null );
            }
            else
            {
                // we are dealing with router
                ResourceStoreRequest request = new ResourceStoreRequest( fromPath, localOnly );

                item = store.retrieveItem( request );
            }
        }
        catch ( AccessDeniedException ex )
        {
            if ( logger != null )
            {
                logger.warn( "Security is enabled. Walking on routers without context is not possible.", ex );
            }
            return;
        }
        catch ( ItemNotFoundException ex )
        {
            if ( logger != null )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "ItemNotFound, finished walking.", ex );
                }
            }
            return;
        }
        catch ( Exception ex )
        {
            if ( logger != null )
            {
                logger.warn( "Got exception on root listing of Store. Finished walking.", ex );
            }
            return;
        }

        int collCount = 0;

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                collCount = walkRecursive( 0, (StorageCollectionItem) item, localOnly, collectionsOnly );
            }
            catch ( Throwable e )
            {
                if ( logger != null )
                {
                    logger.warn( "Walking on " + store.getId() + " store threw an exception " + e.getClass().getName()
                        + " with message (in DEBUG mode the Stack trace is available): " + e.getMessage(), e );
                }

                stop( e );

                throw new WalkerException( e );
            }
        }

        if ( running )
        {
            afterWalk();

            if ( logger != null )
            {
                logger.debug( "Finished walking on " + store.getId() + " Store with " + Integer.toString( collCount )
                    + " collections." );
            }
        }
    }

    protected final int walkRecursive( int collCount, StorageCollectionItem coll, boolean localOnly,
        boolean collectionsOnly )
        throws AccessDeniedException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( !running )
        {
            return collCount;
        }

        boolean shouldProcess = getFilter().shouldProcess( coll );

        boolean shouldProcessRecursively = getFilter().shouldProcessRecursively( coll );

        if ( !shouldProcess && !shouldProcessRecursively )
        {
            return collCount;
        }

        // user may call stop()
        if ( shouldProcess )
        {
            onCollectionEnter( coll );

            collCount++;
        }

        if ( !running )
        {
            return collCount;
        }

        // user may call stop()
        if ( shouldProcess )
        {
            processItem( coll );
        }

        if ( !running )
        {
            return collCount;
        }

        Collection<StorageItem> ls = null;

        if ( shouldProcessRecursively )
        {
            if ( Repository.class.isAssignableFrom( store.getClass() ) )
            {
                ls = ( (Repository) store ).list( coll );
            }
            else
            {
                // we are dealing with router
                ResourceStoreRequest request = new ResourceStoreRequest( coll.getPath(), localOnly );

                ls = store.list( request );
            }

            for ( StorageItem i : ls )
            {
                if ( !collectionsOnly && !StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    if ( getFilter().shouldProcess( i ) )
                    {
                        // user may call stop()
                        processItem( i );
                    }

                    if ( !running )
                    {
                        return collCount;
                    }
                }

                if ( StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    // user may call stop()
                    collCount = walkRecursive( collCount, (StorageCollectionItem) i, localOnly, collectionsOnly );

                    if ( !running )
                    {
                        return collCount;
                    }
                }
            }
        }

        // user may call stop()
        if ( shouldProcess )
        {
            onCollectionExit( coll );
        }

        return collCount;
    }

    protected void beforeWalk()
    {
        // override if needed
    }

    protected void onCollectionEnter( StorageCollectionItem coll )
    {
        // override if needed
    }

    protected abstract void processItem( StorageItem item );

    protected void onCollectionExit( StorageCollectionItem coll )
    {
        // override if needed
    }

    protected void afterWalk()
    {
        // override if needed
    }

}
