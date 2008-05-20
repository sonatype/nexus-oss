package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

public class DeletionNotifierWalker
    extends StoreFileWalker
{
    private Repository repository;

    private Map<String, Object> context;

    public DeletionNotifierWalker( Repository repository, Logger logger, Map<String, Object> context )
    {
        super( repository, logger );

        this.repository = repository;

        this.context = context;
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        if ( context != null )
        {
            item.getItemContext().putAll( context );
        }

        // just fire it, and someone will eventually catch it
        repository.notifyProximityEventListeners( new RepositoryItemEventDelete( item.getRepositoryItemUid(), item
            .getItemContext() ) );
    }

}
