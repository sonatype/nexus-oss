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
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class EvictUnusedItemsWalkerProcessor
    extends AbstractFileWalkerProcessor
{
    private final long timestamp;

    private final ArrayList<String> files;

    public EvictUnusedItemsWalkerProcessor( long timestamp )
    {
        this.timestamp = timestamp;

        this.files = new ArrayList<String>();
    }

    protected Repository getRepository( WalkerContext ctx )
    {
        return (Repository) ctx.getResourceStore();
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
        throws StorageException,
            UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException
    {
        getRepository( ctx ).deleteItem( item.getRepositoryItemUid(), item.getItemContext() );
    }

    @Override
    public void onCollectionExit( WalkerContext ctx, StorageCollectionItem coll )
        throws Exception
    {
        // expiring now empty directories
        try
        {
            if ( getRepository( ctx ).list( coll ).size() == 0 )
            {
                getRepository( ctx ).deleteItem( coll.getRepositoryItemUid(), coll.getItemContext() );
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
}
