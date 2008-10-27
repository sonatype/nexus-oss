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

public class SnapshotRemovalRequest
{
    private final String repositoryId, repositoryGroupId;

    private final int minCountOfSnapshotsToKeep;

    private final int removeSnapshotsOlderThanDays;

    private final boolean removeIfReleaseExists;

    public SnapshotRemovalRequest( String repositoryId, String repositoryGroupId, int minCountOfSnapshotsToKeep,
        int removeSnapshotsOlderThanDays, boolean removeIfReleaseExists )
    {
        super();

        this.repositoryId = repositoryId;

        this.repositoryGroupId = repositoryGroupId;

        this.minCountOfSnapshotsToKeep = minCountOfSnapshotsToKeep;

        this.removeSnapshotsOlderThanDays = removeSnapshotsOlderThanDays;

        this.removeIfReleaseExists = removeIfReleaseExists;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getRepositoryGroupId()
    {
        return repositoryGroupId;
    }

    public int getMinCountOfSnapshotsToKeep()
    {
        return minCountOfSnapshotsToKeep;
    }

    public int getRemoveSnapshotsOlderThanDays()
    {
        return removeSnapshotsOlderThanDays;
    }

    public boolean isRemoveIfReleaseExists()
    {
        return removeIfReleaseExists;
    }
}
