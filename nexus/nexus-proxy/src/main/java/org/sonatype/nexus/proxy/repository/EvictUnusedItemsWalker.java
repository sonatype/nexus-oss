package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

public class EvictUnusedItemsWalker
    extends StoreFileWalker
{
    private final Repository repository;

    private final long timestamp;

    private final ArrayList<String> files;

    public EvictUnusedItemsWalker( Repository repository, Logger logger, long timestamp )
    {
        super( repository, logger );

        this.repository = repository;

        this.timestamp = timestamp;

        this.files = new ArrayList<String>();
    }

    protected Repository getRepository()
    {
        return repository;
    }

    public List<String> getFiles()
    {
        return files;
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        // expiring found files
        try
        {
            if ( item.getLastRequested() < timestamp )
            {
                getRepository().deleteItem( item.getRepositoryItemUid() );

                files.add( item.getPath() );
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
            getLogger().warn( "Got storage exception while evicting " + item.getRepositoryItemUid().toString(), e );
        }
    }

    @Override
    protected void onCollectionExit( StorageCollectionItem coll )
    {
        // expiring now empty directories
        try
        {
            if ( ( (Repository) getResourceStore() ).list( coll ).size() == 0 )
            {
                getRepository().deleteItem( coll.getRepositoryItemUid() );
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
