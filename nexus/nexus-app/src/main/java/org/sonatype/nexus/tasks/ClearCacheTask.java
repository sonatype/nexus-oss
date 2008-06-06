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

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * Clear caches task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.tasks.ClearCacheTask"
 */
public class ClearCacheTask
    extends AbstractNexusRepositoriesTask<Object>
{
    private String resourceStorePath;

    public String getResourceStorePath()
    {
        return resourceStorePath;
    }

    public void setResourceStorePath( String resourceStorePath )
    {
        this.resourceStorePath = resourceStorePath;
    }

    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().clearRepositoryGroupCaches( resourceStorePath, getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().clearRepositoryCaches( resourceStorePath, getRepositoryId() );
        }
        else
        {
            getNexus().clearAllCaches( resourceStorePath );
        }

        return null;
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_CLEARCACHE_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Clearing caches for repository group with ID=" + getRepositoryGroupId() + " from path "
                + resourceStorePath + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Clearing caches for repository with ID=" + getRepositoryId() + " from path " + resourceStorePath
                + " and below.";
        }
        else
        {
            return "Clearing caches for all registered repositories" + " from path " + resourceStorePath
                + " and below.";
        }
    }

}
