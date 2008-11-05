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
package org.sonatype.nexus.proxy.walker;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class Walker.
 * 
 * @author cstamas
 */
@Component( role = Walker.class )
public class DefaultWalker
    extends AbstractLogEnabled
    implements Walker
{
    public void walk( WalkerContext context )
        throws WalkerException
    {
        walk( context, null );
    }

    public void walk( WalkerContext context, String fromPath )
        throws WalkerException
    {
        walk( context, fromPath, true, false );
    }

    public final void walk( WalkerContext context, boolean localOnly, boolean collectionsOnly )
        throws WalkerException
    {
        walk( context, null, localOnly, collectionsOnly );
    }

    public final void walk( WalkerContext context, String fromPath, boolean localOnly, boolean collectionsOnly )
        throws WalkerException
    {
        if ( fromPath == null )
        {
            fromPath = RepositoryItemUid.PATH_ROOT;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Start walking on ResourceStore " + context.getResourceStore().getId() + " from path '" + fromPath
                    + "'." );
        }

        // user may call stop()
        beforeWalk( context );

        if ( context.isStopped() )
        {
            return;
        }

        StorageItem item = null;

        try
        {
            if ( Repository.class.isAssignableFrom( context.getResourceStore().getClass() ) )
            {
                // we are dealing with repository
                // this way we avoid security context processing!!!
                // TODO: enable somehow ability to pass-over the req context!
                RepositoryItemUid uid = ( (Repository) context.getResourceStore() ).createUid( fromPath );

                item = ( (Repository) context.getResourceStore() ).retrieveItem( localOnly, uid, null );
            }
            else
            {
                // we are dealing with router
                ResourceStoreRequest request = new ResourceStoreRequest( fromPath, localOnly );

                item = context.getResourceStore().retrieveItem( request );
            }
        }
        catch ( AccessDeniedException ex )
        {
            getLogger().warn( "Security is enabled. Walking on routers without context is not possible.", ex );

            return;
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "ItemNotFound where walking should start, bailing out.", ex );
            }

            return;
        }
        catch ( Exception ex )
        {
            getLogger().warn( "Got exception while doing retrieve, bailing out.", ex );

            return;
        }

        int collCount = 0;

        if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                WalkerFilter filter = context.getFilter() != null
                    ? context.getFilter()
                    : new AffirmativeStoreWalkerFilter();

                collCount = walkRecursive( 0, context, filter, (StorageCollectionItem) item, localOnly, collectionsOnly );
            }
            catch ( Throwable e )
            {
                context.stop( e );

                throw new WalkerException( context, e );
            }
        }

        if ( !context.isStopped() )
        {
            afterWalk( context );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Start walking on ResourceStore " + context.getResourceStore().getId() + " from path '" + fromPath
                        + "'. Walked over " + Integer.toString( collCount ) + " collections." );

            }
        }
    }

    protected final int walkRecursive( int collCount, WalkerContext context, WalkerFilter filter,
        StorageCollectionItem coll, boolean localOnly, boolean collectionsOnly )
        throws AccessDeniedException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            NoSuchResourceStoreException,
            StorageException
    {
        if ( context.isStopped() )
        {
            return collCount;
        }

        boolean shouldProcess = filter.shouldProcess( context, coll );

        boolean shouldProcessRecursively = filter.shouldProcessRecursively( context, coll );

        if ( !shouldProcess && !shouldProcessRecursively )
        {
            return collCount;
        }

        // user may call stop()
        if ( shouldProcess )
        {
            onCollectionEnter( context, coll );

            collCount++;
        }

        if ( context.isStopped() )
        {
            return collCount;
        }

        // user may call stop()
        if ( shouldProcess )
        {
            processItem( context, coll );
        }

        if ( context.isStopped() )
        {
            return collCount;
        }

        Collection<StorageItem> ls = null;

        if ( shouldProcessRecursively )
        {
            if ( Repository.class.isAssignableFrom( context.getResourceStore().getClass() ) )
            {
                ls = ( (Repository) context.getResourceStore() ).list( coll );
            }
            else
            {
                // we are dealing with router
                ResourceStoreRequest request = new ResourceStoreRequest( coll.getPath(), localOnly );

                ls = context.getResourceStore().list( request );
            }

            for ( StorageItem i : ls )
            {
                if ( !collectionsOnly && !StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    if ( filter.shouldProcess( context, i ) )
                    {
                        // user may call stop()
                        processItem( context, i );
                    }

                    if ( context.isStopped() )
                    {
                        return collCount;
                    }
                }

                if ( StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
                {
                    // user may call stop()
                    collCount = walkRecursive(
                        collCount,
                        context,
                        filter,
                        (StorageCollectionItem) i,
                        localOnly,
                        collectionsOnly );

                    if ( context.isStopped() )
                    {
                        return collCount;
                    }
                }
            }
        }

        // user may call stop()
        if ( shouldProcess )
        {
            onCollectionExit( context, coll );
        }

        return collCount;
    }

    protected void beforeWalk( WalkerContext context )
    {
        try
        {
            for ( WalkerProcessor processor : context.getProcessors() )
            {
                processor.beforeWalk( context );

                if ( context.isStopped() )
                {
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

    protected void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
    {
        try
        {
            for ( WalkerProcessor processor : context.getProcessors() )
            {
                processor.onCollectionEnter( context, coll );

                if ( context.isStopped() )
                {
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

    protected void processItem( WalkerContext context, StorageItem item )
    {
        try
        {
            for ( WalkerProcessor processor : context.getProcessors() )
            {
                processor.processItem( context, item );

                if ( context.isStopped() )
                {
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

    protected void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
    {
        try
        {
            for ( WalkerProcessor processor : context.getProcessors() )
            {
                processor.onCollectionExit( context, coll );

                if ( context.isStopped() )
                {
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

    protected void afterWalk( WalkerContext context )
    {
        try
        {
            for ( WalkerProcessor processor : context.getProcessors() )
            {
                processor.afterWalk( context );

                if ( context.isStopped() )
                {
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

}
