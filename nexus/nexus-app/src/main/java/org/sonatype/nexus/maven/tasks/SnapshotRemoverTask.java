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
package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * SnapshotRemoverTask
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.maven.tasks.SnapshotRemoverTask"
 */
public class SnapshotRemoverTask
    extends AbstractNexusRepositoriesTask<SnapshotRemovalResult>
{
    private int minSnapshotsToKeep;

    private int removeOlderThanDays;

    private boolean removeIfReleaseExists;

    public int getMinSnapshotsToKeep()
    {
        return minSnapshotsToKeep;
    }

    public int getRemoveOlderThanDays()
    {
        return removeOlderThanDays;
    }

    public boolean isRemoveIfReleaseExists()
    {
        return removeIfReleaseExists;
    }

    public SnapshotRemovalResult doRun()
        throws Exception
    {
        SnapshotRemovalRequest req = new SnapshotRemovalRequest(
            getRepositoryId(),
            getRepositoryGroupId(),
            minSnapshotsToKeep,
            removeOlderThanDays,
            removeIfReleaseExists );

        return getNexus().removeSnapshots( req );
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REMOVE_SNAPSHOTS_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Removing snapshots from repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Removing snapshots from repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Removing snapshots from all registered repositories";
        }
    }

}
