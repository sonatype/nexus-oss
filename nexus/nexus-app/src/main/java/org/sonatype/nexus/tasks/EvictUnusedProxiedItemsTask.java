/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.tasks;

import java.util.Collection;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * Evicts unused proxied items.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.scheduling.SchedulerTask"
 *                   role-hint="org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask"
 *                   instantiation-strategy="per-lookup"
 */
public class EvictUnusedProxiedItemsTask
    extends AbstractNexusRepositoriesTask<Collection<String>>
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
    protected Collection<String> doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            return getNexus().evictRepositoryGroupUnusedProxiedItems(
                System.currentTimeMillis() - ( ( (long) getEvictOlderCacheItemsThen() ) * A_DAY ),
                getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            return getNexus().evictRepositoryUnusedProxiedItems(
                System.currentTimeMillis() - ( ( (long) getEvictOlderCacheItemsThen() ) * A_DAY ),
                getRepositoryId() );
        }
        else
        {
            return getNexus().evictAllUnusedProxiedItems(
                System.currentTimeMillis() - ( ( (long) getEvictOlderCacheItemsThen() ) * A_DAY ) );
        }
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
