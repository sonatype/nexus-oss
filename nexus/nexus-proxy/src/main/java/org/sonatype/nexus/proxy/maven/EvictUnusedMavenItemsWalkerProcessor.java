package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalkerProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

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
        implements WalkerFilter
    {
        public boolean shouldProcess( WalkerContext context, StorageItem item )
        {
            return !item.getPath().startsWith( "/.index" ) && !item.getPath().endsWith( ".asc" )
                && !item.getPath().endsWith( ".sha1" ) && !item.getPath().endsWith( ".md5" );
        }

        public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
        {
            // we are "cutting" the .index dir from processing
            return shouldProcess( context, coll );
        }
    }

    @Override
    public void doDelete( WalkerContext ctx, StorageFileItem item )
        throws StorageException,
            UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException
    {
        MavenRepository repository = (MavenRepository) getRepository( ctx );

        repository.deleteItemWithChecksums( item.getRepositoryItemUid(), null );
    }

    // on maven repositories, we must use another delete method
    @Override
    public void onCollectionExit( WalkerContext ctx, StorageCollectionItem coll )
    {
        // expiring now empty directories
        try
        {
            if ( getRepository( ctx ).list( coll ).size() == 0 )
            {
                ( (MavenRepository) getRepository( ctx ) ).deleteItemWithChecksums( coll.getRepositoryItemUid(), coll
                    .getItemContext() );
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
