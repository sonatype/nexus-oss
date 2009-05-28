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
package org.sonatype.nexus.proxy.walker;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

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
    public static final String WALKER_WALKED_COLLECTION_COUNT = Walker.class.getSimpleName() + ".collCount";

    public static final String WALKER_WALKED_FROM_PATH = Walker.class.getSimpleName() + ".fromPath";

    public void walk( WalkerContext context )
        throws WalkerException
    {
        String fromPath = context.getResourceStoreRequest().getRequestPath();

        if ( fromPath == null )
        {
            fromPath = RepositoryItemUid.PATH_ROOT;
        }

        context.getContext().put( WALKER_WALKED_FROM_PATH, fromPath );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Start walking on ResourceStore " + context.getRepository().getId() + " from path '" + fromPath + "'." );
        }

        // user may call stop()
        beforeWalk( context );

        if ( context.isStopped() )
        {
            reportWalkEnd( context, fromPath );

            return;
        }

        StorageItem item = null;

        try
        {
            item = context.getRepository().retrieveItem( true, context.getResourceStoreRequest() );
        }
        catch ( ItemNotFoundException ex )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "ItemNotFound where walking should start, bailing out.", ex );
            }

            context.stop( ex );

            reportWalkEnd( context, fromPath );

            return;
        }
        catch ( Exception ex )
        {
            getLogger().warn( "Got exception while doing retrieve, bailing out.", ex );

            context.stop( ex );

            reportWalkEnd( context, fromPath );

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

                collCount = walkRecursive( 0, context, filter, (StorageCollectionItem) item );

                context.getContext().put( WALKER_WALKED_COLLECTION_COUNT, collCount );
            }
            catch ( Throwable e )
            {
                context.stop( e );

                reportWalkEnd( context, fromPath );

                return;
            }
        }

        if ( !context.isStopped() )
        {
            afterWalk( context );
        }

        reportWalkEnd( context, fromPath );
    }

    protected void reportWalkEnd( WalkerContext context, String fromPath )
        throws WalkerException
    {
        if ( context.isStopped() )
        {
            // we have a cause, report any non-ItemNotFounds with stack trace
            if ( context.getStopCause() instanceof ItemNotFoundException )
            {
                getLogger().info(
                    "Aborted walking on repository ID='" + context.getRepository().getId() + "' from path='"
                        + fromPath + "', cause: " + context.getStopCause().getMessage() );
            }
            else
            {
                getLogger().info(
                    "Aborted walking on repository ID='" + context.getRepository().getId() + "' from path='" + fromPath
                        + "', cause:",
                    context.getStopCause() );
            }

            throw new WalkerException( context, "Aborted walking on repository ID='" + context.getRepository().getId()
                + "' from path='" + fromPath + "'." );
        }
        else
        {
            // regular finish, it was not stopped
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Finished walking on ResourceStore '" + context.getRepository().getId() + "' from path '"
                        + context.getContext().get( WALKER_WALKED_FROM_PATH ) + "'." );
            }
        }
    }

    protected final int walkRecursive( int collCount, WalkerContext context, WalkerFilter filter,
        StorageCollectionItem coll )
        throws AccessDeniedException,
            IllegalOperationException,
            ItemNotFoundException,
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
            ls = context.getRepository().list( false, coll );

            for ( StorageItem i : ls )
            {
                if ( !context.isCollectionsOnly() && !StorageCollectionItem.class.isAssignableFrom( i.getClass() ) )
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
                    collCount = walkRecursive( collCount, context, filter, (StorageCollectionItem) i );

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
                if ( processor.isActive() )
                {
                    processor.beforeWalk( context );

                    if ( context.isStopped() )
                    {
                        break;
                    }
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
                if ( processor.isActive() )
                {
                    processor.onCollectionEnter( context, coll );

                    if ( context.isStopped() )
                    {
                        break;
                    }
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
                if ( processor.isActive() )
                {
                    processor.processItem( context, item );

                    if ( context.isStopped() )
                    {
                        break;
                    }
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
                if ( processor.isActive() )
                {
                    processor.onCollectionExit( context, coll );

                    if ( context.isStopped() )
                    {
                        break;
                    }
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
                if ( processor.isActive() )
                {
                    processor.afterWalk( context );

                    if ( context.isStopped() )
                    {
                        break;
                    }
                }
            }
        }
        catch ( Exception e )
        {
            context.stop( e );
        }
    }

}
