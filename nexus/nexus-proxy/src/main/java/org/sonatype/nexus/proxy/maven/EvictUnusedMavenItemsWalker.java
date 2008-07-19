package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalker;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.StoreWalkerFilter;

public class EvictUnusedMavenItemsWalker
    extends EvictUnusedItemsWalker
{
    public EvictUnusedMavenItemsWalker( Repository repository, Logger logger, long timestamp )
    {
        super( repository, logger, timestamp );

        setFilter( new EvictUnusedMavenItemsWalkerFilter() );
    }

    public class EvictUnusedMavenItemsWalkerFilter
        implements StoreWalkerFilter
    {
        public boolean shouldProcess( StorageItem item )
        {
            return !item.getPath().startsWith( "/.index" );
        }

        public boolean shouldProcessRecursively( StorageCollectionItem coll )
        {
            // we are "cutting" the .index dir from processing
            return shouldProcess( coll );
        }
    }
}
