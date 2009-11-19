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
import java.util.List;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

public class EvictUnusedItemsWalkerProcessor
    extends AbstractFileWalkerProcessor
{
    public static final String REQUIRED_FACET_KEY = "repository.facet";

    public static final Class<? extends Repository> DEFAULT_REQUIRED_FACET = ProxyRepository.class;

    private final long timestamp;

    private final ArrayList<String> files;

    public EvictUnusedItemsWalkerProcessor( long timestamp )
    {
        this.timestamp = timestamp;

        this.files = new ArrayList<String>();
    }

    protected Class<? extends Repository> getRequiredFacet( WalkerContext context )
    {
        if ( context.getContext().containsKey( REQUIRED_FACET_KEY ) )
        {
            return (Class<? extends Repository>) context.getContext().get( REQUIRED_FACET_KEY );
        }
        else
        {
            return DEFAULT_REQUIRED_FACET;
        }
    }

    protected Repository getRepository( WalkerContext ctx )
    {
        return ctx.getRepository();
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public List<String> getFiles()
    {
        return files;
    }

    @Override
    public void beforeWalk( WalkerContext context )
        throws Exception
    {
        Class<? extends Repository> requiredFacet = getRequiredFacet( context );

        if ( !getRepository( context ).getRepositoryKind().isFacetAvailable( requiredFacet ) )
        {
            context.stop( null );
        }
    }

    @Override
    public void processFileItem( WalkerContext ctx, StorageFileItem item )
        throws StorageException
    {
        // expiring found files
        try
        {
            if ( item.getLastRequested() < getTimestamp() )
            {
                doDelete( ctx, item );

                getFiles().add( item.getPath() );
            }
        }
        catch ( IllegalOperationException e )
        {
            // simply stop if set during processing
            ctx.stop( e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // if op not supported (R/O repo?)
            ctx.stop( e );
        }
        catch ( ItemNotFoundException e )
        {
            // will not happen
        }
    }

    protected void doDelete( WalkerContext ctx, StorageFileItem item )
        throws StorageException, UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException
    {
        getRepository( ctx ).deleteItem( false, new ResourceStoreRequest( item ) );
    }

    @Override
    public void onCollectionExit( WalkerContext ctx, StorageCollectionItem coll )
        throws Exception
    {
        // expiring now empty directories
        try
        {
            if ( getRepository( ctx ).list( false, coll ).size() == 0 )
            {
                getRepository( ctx ).deleteItem( false, new ResourceStoreRequest( coll ) );
            }
        }
        catch ( RepositoryNotAvailableException e )
        {
            // simply stop if set during processing
            ctx.stop( e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // if op not supported (R/O repo?)
            ctx.stop( e );
        }
        catch ( ItemNotFoundException e )
        {
            // will not happen
        }
        catch ( StorageException e )
        {
            ctx.stop( e );
        }
    }

    // ==

    public static class EvictUnusedItemsWalkerFilter
        implements WalkerFilter
    {
        public boolean shouldProcess( WalkerContext context, StorageItem item )
        {
            // skip "hidden" files
            return !item.getPath().startsWith( "/." ) && !item.getPath().startsWith( "." );
        }

        public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
        {
            // we are "cutting" the .index dir from processing
            return shouldProcess( context, coll );
        }
    }

}
