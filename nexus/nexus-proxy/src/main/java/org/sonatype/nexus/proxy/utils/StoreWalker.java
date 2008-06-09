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
package org.sonatype.nexus.proxy.utils;

import java.util.Collection;
import java.util.Stack;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class StoreWalker.
 * 
 * @author cstamas
 */
public abstract class StoreWalker
{
    protected ResourceStore store;

    protected Logger logger;

    protected boolean running;

    public StoreWalker( ResourceStore store, Logger logger )
    {
        super();

        this.store = store;

        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected ResourceStore getResourceStore()
    {
        return store;
    }

    public void walk()
    {
        walk( null );
    }

    public void walk( String fromPath )
    {
        walk( fromPath, true, false );
    }

    public final void walk( boolean localOnly, boolean collectionsOnly )
    {
        walk( null, localOnly, collectionsOnly );
    }

    public void stop()
    {
        running = false;
    }

    public final void walk( String fromPath, boolean localOnly, boolean collectionsOnly )
    {
        running = true;

        if ( fromPath == null )
        {
            fromPath = RepositoryItemUid.PATH_ROOT;
        }

        int itemCount = 0;

        if ( logger != null && logger.isDebugEnabled() )
        {
            logger.debug( "Start walking on ResourceStore " + store.getId() + " from path " + fromPath );
        }

        beforeWalk();

        Stack<StorageCollectionItem> stack = new Stack<StorageCollectionItem>();
        StorageItem item = null;
        StorageCollectionItem coll = null;

        try
        {
            if ( Repository.class.isAssignableFrom( store.getClass() ) )
            {
                // we are dealing with repository
                // this way we avoid security context processing
                RepositoryItemUid uid = new RepositoryItemUid( (Repository) store, fromPath );

                item = ( (Repository) store ).retrieveItem( localOnly, uid );
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

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            stack.push( (StorageCollectionItem) item );
        }
        while ( !stack.isEmpty() && running )
        {
            coll = stack.pop();

            processItem( coll );

            onCollectionEnter( coll );

            itemCount++;

            Collection<StorageItem> ls = coll.list();

            for ( StorageItem i : ls )
            {
                if ( collectionsOnly && StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    processItem( i );
                }
                else
                {
                    processItem( i );
                }
                if ( StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    stack.push( (StorageCollectionItem) i );
                }
            }

            onCollectionExit( coll );
        }

        afterWalk();

        if ( running )
        {
            if ( logger != null )
            {
                logger.info( "Finished walking on " + store.getId() + " Store with " + Integer.toString( itemCount )
                    + " items" );
            }
        }
        else
        {
            if ( logger != null )
            {
                logger.info( "Walking STOPPED on " + store.getId() + " Store with " + Integer.toString( itemCount )
                    + " items" );
            }
        }
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
