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
package org.sonatype.nexus.proxy.walker;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.scheduling.TaskInterruptedException;

/**
 * The Class Walker.
 * 
 * @author cstamas
 */
@Component( role = Walker.class )
public class DefaultWalker
    extends AbstractLoggingComponent
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

        // cannot walk out of service repos
        if( LocalStatus.OUT_OF_SERVICE == context.getRepository().getLocalStatus() )
        {
            getLogger().info( "Cannot walk, repository: '"+ context.getRepository().getId() + "' is out of service." );
        }
        else
        {
            context.getContext().put( WALKER_WALKED_FROM_PATH, fromPath );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Start walking on ResourceStore " + context.getRepository().getId() + " from path '" + fromPath + "'." );
            }

            try
            {
                // user may call stop()
                beforeWalk( context );

                if ( context.isStopped() )
                {
                    reportWalkEnd( context, fromPath );

                    return;
                }

                StorageItem item = null;

                item = context.getRepository().retrieveItem( true, context.getResourceStoreRequest() );

                int collCount = 0;

                if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    try
                    {
                        WalkerFilter filter =
                            context.getFilter() != null ? context.getFilter() : new DefaultStoreWalkerFilter();

                        collCount = walkRecursive( 0, context, filter, (StorageCollectionItem) item );

                        context.getContext().put( WALKER_WALKED_COLLECTION_COUNT, collCount );
                    }
                    catch ( Exception e )
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
            }
            catch ( ItemNotFoundException ex )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "ItemNotFound where walking should start, bailing out.", ex );
                }

                context.stop( ex );
            }
            catch ( Exception ex )
            {
                getLogger().warn( "Got exception while doing retrieve, bailing out.", ex );

                context.stop( ex );
            }
        }
        reportWalkEnd( context, fromPath );
    }

    protected void reportWalkEnd( WalkerContext context, String fromPath )
        throws WalkerException
    {
        if ( context.isStopped() )
        {
            if ( context.getStopCause() == null )
            {
                getLogger().debug( "Walker was stopped programatically, not because of error." );
            }
            else if ( context.getStopCause() instanceof TaskInterruptedException )
            {
                getLogger().info(
                    RepositoryStringUtils.getFormattedMessage( "Canceled walking on repository %s from path=\""
                        + fromPath + "\", cause: " + context.getStopCause().getMessage(), context.getRepository() ) );
            }
            else
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
                        "Aborted walking on repository ID='" + context.getRepository().getId() + "' from path='"
                            + fromPath + "', cause:", context.getStopCause() );
                }

                throw new WalkerException( context, "Aborted walking on repository ID='"
                    + context.getRepository().getId() + "' from path='" + fromPath + "'." );
            }
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
        throws AccessDeniedException, IllegalOperationException, ItemNotFoundException, StorageException
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
