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
    public void walk( ResourceStore store, Logger logger )
    {
        walk( store, logger, null );
    }

    public void walk( ResourceStore store, Logger logger, String fromPath )
    {
        walk( store, logger, fromPath, true, false );
    }

    public final void walk( ResourceStore store, Logger logger, boolean localOnly, boolean collectionsOnly )
    {
        walk( store, logger, null, localOnly, collectionsOnly );
    }

    public final void walk( ResourceStore store, Logger logger, String fromPath, boolean localOnly,
        boolean collectionsOnly )
    {
        if ( fromPath == null )
        {
            fromPath = RepositoryItemUid.PATH_ROOT;
        }

        int itemCount = 0;

        if ( logger != null && logger.isDebugEnabled() )
        {
            logger.debug( "Start walking on ResourceStore " + store.getId() + " from path " + fromPath );
        }

        beforeWalk( store, logger );

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
        while ( !stack.isEmpty() )
        {
            coll = stack.pop();
            processItem( store, coll, logger );
            onCollectionEnter( store, coll, logger );
            itemCount++;
            Collection<StorageItem> ls = coll.list();
            for ( StorageItem i : ls )
            {
                if ( collectionsOnly && StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    processItem( store, i, logger );
                }
                else
                {
                    processItem( store, i, logger );
                }
                if ( StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    stack.push( (StorageCollectionItem) i );
                }
            }
            onCollectionExit( store, coll, logger );
        }
        afterWalk( store, logger );
        if ( logger != null )
        {
            logger.info( "Finished walking on " + store.getId() + " Store with " + Integer.toString( itemCount )
                + " items" );
        }
    }

    protected void beforeWalk( ResourceStore store, Logger logger )
    {
        // override if needed
    }

    protected void onCollectionEnter( ResourceStore store, StorageCollectionItem coll, Logger logger )
    {
        // override if needed
    }

    protected abstract void processItem( ResourceStore store, StorageItem item, Logger logger );

    protected void onCollectionExit( ResourceStore store, StorageCollectionItem coll, Logger logger )
    {
        // override if needed
    }

    protected void afterWalk( ResourceStore store, Logger logger )
    {
        // override if needed
    }

}
