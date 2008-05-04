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
package org.sonatype.nexus.rest.cache;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.rest.AbstractRestTask;

public class ClearCacheTask
    extends AbstractRestTask<Object>
{
    private final String repositoryId;

    private final String repositoryGroupId;

    private final String resourceStorePath;

    public ClearCacheTask( Nexus nexus, String repositoryId, String repositoryGroupId, String resourceStorePath )
    {
        super( nexus );

        this.repositoryId = repositoryId;

        this.repositoryGroupId = repositoryGroupId;

        this.resourceStorePath = resourceStorePath;
    }

    public Object doRun()
        throws Exception
    {
        getNexus().clearCaches( resourceStorePath, repositoryId, repositoryGroupId );

        return null;
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_CLEARCACHE_ACTION;
    }

    protected String getMessage()
    {
        if ( repositoryGroupId != null )
        {
            return "Clearing caches for repository group with ID=" + repositoryGroupId + " from path "
                + resourceStorePath + " and below.";
        }
        else if ( repositoryId != null )
        {
            return "Clearing caches for repository with ID=" + repositoryId + " from path " + resourceStorePath
                + " and below.";
        }
        else
        {
            return "Clearing caches for all registered repositories" + " from path " + resourceStorePath
                + " and below.";
        }
    }

}
