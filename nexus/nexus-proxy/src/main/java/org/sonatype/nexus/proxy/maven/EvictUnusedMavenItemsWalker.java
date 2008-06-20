package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.repository.EvictUnusedItemsWalker;
import org.sonatype.nexus.proxy.repository.Repository;

public class EvictUnusedMavenItemsWalker
    extends EvictUnusedItemsWalker
{
    public EvictUnusedMavenItemsWalker( Repository repository, Logger logger, long timestamp )
    {
        super( repository, logger, timestamp );
    }

    public boolean shouldProcess( StorageCollectionItem coll )
    {
        // exclude the index dirs
        return !coll.getPath().startsWith( "/.index" );
    }
}
