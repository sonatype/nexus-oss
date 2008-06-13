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

import java.io.IOException;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.tasks.PublishIndexesTask" instantiation-strategy="per-lookup"
 */
public class PublishIndexesTask
    extends AbstractNexusRepositoriesTask<Object>
{
    @Override
    protected Object doRun()
        throws Exception
    {
        try
        {
            if ( getRepositoryId() != null )
            {
                getNexus().publishRepositoryIndex( getRepositoryId() );
            }
            else if ( getRepositoryGroupId() != null )
            {
                getNexus().publishRepositoryGroupIndex( getRepositoryGroupId() );
            }
            else
            {
                getNexus().publishAllIndex();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot publish indexes!", e );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_PUBLISHINDEX_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Publishing indexes for all registered repositories.";
    }

}
