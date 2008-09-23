package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalker;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.StoreWalkerFilter;

public class EvictUnusedMavenItemsWalker
    extends EvictUnusedItemsWalker
{
    public EvictUnusedMavenItemsWalker( MavenRepository repository, Logger logger, long timestamp )
    {
        super( repository, logger, timestamp );

        setFilter( new EvictUnusedMavenItemsWalkerFilter() );
    }

    // added filter for maven reposes to exclude .index dirs
    // and all hash files, as they will be removed if main artifact
    // is removed
    public class EvictUnusedMavenItemsWalkerFilter
        implements StoreWalkerFilter
    {
        public boolean shouldProcess( StorageItem item )
        {
            return !item.getPath().startsWith( "/.index" )
                && !item.getPath().endsWith( ".asc" )
                && !item.getPath().endsWith( ".sha1" )
                && !item.getPath().endsWith( ".md5" );
        }

        public boolean shouldProcessRecursively( StorageCollectionItem coll )
        {
            // we are "cutting" the .index dir from processing
            return shouldProcess( coll );
        }
    }
    
    @Override
    protected void doDelete( StorageFileItem item ) 
        throws StorageException, 
            UnsupportedStorageOperationException, 
            RepositoryNotAvailableException, 
            ItemNotFoundException
    {
        MavenRepository repository = ( MavenRepository ) getRepository();
        
        repository.deleteItemWithChecksums( item.getRepositoryItemUid(), null );
    }

    // on maven repositories, we must use another delete method
    @Override
    protected void onCollectionExit( StorageCollectionItem coll )
    {
        // expiring now empty directories
        try
        {
            if ( ( (MavenRepository) getResourceStore() ).list( coll ).size() == 0 )
            {
                ( (MavenRepository) getRepository() ).deleteItemWithChecksums( coll.getRepositoryItemUid(), coll
                    .getItemContext() );
            }
        }
        catch ( RepositoryNotAvailableException e )
        {
            // simply stop if set during processing
            stop( e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // if op not supported (R/O repo?)
            stop( e );
        }
        catch ( ItemNotFoundException e )
        {
            // will not happen
        }
        catch ( StorageException e )
        {
            stop( e );
        }
    }

}
