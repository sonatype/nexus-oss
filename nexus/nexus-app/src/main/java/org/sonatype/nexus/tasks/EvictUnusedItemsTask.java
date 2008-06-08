package org.sonatype.nexus.tasks;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

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
        // walk the proxy repo (check for type, skip non-proxy reposes), and delete all items that has lastTouched older
        // than given days

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
            return "Evicting unused proxied items for repository group with ID=" + getRepositoryGroupId() + ".";
        }
        else if ( getRepositoryId() != null )
        {
            return "Evicting unused proxied items for repository with ID=" + getRepositoryId() + ".";
        }
        else
        {
            return "Evicting unused proxied items for all registered proxy repositories.";
        }
    }

}
