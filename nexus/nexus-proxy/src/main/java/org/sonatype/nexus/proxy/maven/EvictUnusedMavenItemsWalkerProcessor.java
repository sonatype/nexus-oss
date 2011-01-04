/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalkerProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class EvictUnusedMavenItemsWalkerProcessor
    extends EvictUnusedItemsWalkerProcessor
{
    public EvictUnusedMavenItemsWalkerProcessor( long timestamp )
    {
        super( timestamp );
    }

    // added filter for maven reposes to exclude .index dirs
    // and all hash files, as they will be removed if main artifact
    // is removed
    public static class EvictUnusedMavenItemsWalkerFilter
        extends EvictUnusedItemsWalkerFilter
    {
        public boolean shouldProcess( WalkerContext context, StorageItem item )
        {
            return super.shouldProcess( context, item ) && !item.getPath().startsWith( "/.index" )
                && !item.getPath().endsWith( ".asc" ) && !item.getPath().endsWith( ".sha1" )
                && !item.getPath().endsWith( ".md5" );
        }
    }

    @Override
    public void doDelete( WalkerContext ctx, StorageFileItem item )
        throws StorageException, UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException
    {
        MavenRepository repository = (MavenRepository) getRepository( ctx );

        repository.deleteItemWithChecksums( false, new ResourceStoreRequest( item ) );
    }

    // on maven repositories, we must use another delete method
    @Override
    public void onCollectionExit( WalkerContext ctx, StorageCollectionItem coll )
    {
        // expiring now empty directories
        try
        {
            if ( getRepository( ctx ).list( false, coll ).size() == 0 )
            {
                ( (MavenRepository) getRepository( ctx ) ).deleteItemWithChecksums( false, new ResourceStoreRequest(
                    coll ) );
            }
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
        catch ( IllegalOperationException e )
        {
            // simply stop if set during processing
            ctx.stop( e );
        }
        catch ( StorageException e )
        {
            ctx.stop( e );
        }
    }
}
