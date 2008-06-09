package org.sonatype.nexus.tasks;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * Evicts unused proxied items.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.tasks.EvictUnusedItemsTask"
 */
public class EvictUnusedItemsTask
    extends AbstractNexusRepositoriesTask<Object>
{
    public static final String EVICT_OLDER_CACHE_ITEMS_THEN_KEY = "evictOlderCacheItemsThen";

    public int getEvictOlderCacheItemsThen()
    {
        return Integer.parseInt( getParameters().get( EVICT_OLDER_CACHE_ITEMS_THEN_KEY ) );
    }

    public void setEvictOlderCacheItemsThen( int evictOlderCacheItemsThen )
    {
        getParameters().put( EVICT_OLDER_CACHE_ITEMS_THEN_KEY, Integer.toString( evictOlderCacheItemsThen ) );
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().evictRepositoryGroupUnusedItems( getEvictOlderCacheItemsThen(), getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().evictRepositoryUnusedItems( getEvictOlderCacheItemsThen(), getRepositoryId() );
        }
        else
        {
            getNexus().evictAllUnusedItems( getEvictOlderCacheItemsThen() );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_EVICT_UNUSED_PROXIED_ITEMS;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Evicting unused items for repository group with ID=" + getRepositoryGroupId() + ".";
        }
        else if ( getRepositoryId() != null )
        {
            return "Evicting unused items for repository with ID=" + getRepositoryId() + ".";
        }
        else
        {
            return "Evicting unused items for all registered repositories.";
        }
    }

}
