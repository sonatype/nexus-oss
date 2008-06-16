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
 * Rebuild attributes task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.scheduling.NexusTask"
 *                   role-hint="org.sonatype.nexus.tasks.RebuildAttributesTask" instantiation-strategy="per-lookup"
 */
public class RebuildAttributesTask
    extends AbstractNexusRepositoriesTask<Object>
{
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().rebuildAttributesRepositoryGroup( getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().rebuildAttributesRepository( getRepositoryId() );
        }
        else
        {
            getNexus().rebuildAttributesAllRepositories();
        }

        return null;
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REBUILDATTRIBUTES_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Rebuilding Attributes of repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Rebuilding Attributes of repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Rebuilding Attributes of all registered repositories";
        }
    }

}
