/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.tasks;

import java.util.HashSet;
import java.util.Set;

public class SnapshotRemovalRequest
{
    private final String repositoryId;

    private final int minCountOfSnapshotsToKeep;

    private final int removeSnapshotsOlderThanDays;

    private final boolean removeIfReleaseExists;

    private final Set<String> processedRepos;

    private final boolean deleteImmediately;

    /**
     * Old behavior without changing trash or delete (always trash).
     * <p/>
     * (see NEXUS-4579)
     */
    public SnapshotRemovalRequest( String repositoryId, int minCountOfSnapshotsToKeep,
                                   int removeSnapshotsOlderThanDays, boolean removeIfReleaseExists )
    {

        this( repositoryId, minCountOfSnapshotsToKeep, removeSnapshotsOlderThanDays, removeIfReleaseExists, false );
    }

    public SnapshotRemovalRequest( String repositoryId, int minCountOfSnapshotsToKeep,
                                   int removeSnapshotsOlderThanDays, boolean removeIfReleaseExists,
                                   boolean deleteImmediately )
    {
        this.repositoryId = repositoryId;

        this.minCountOfSnapshotsToKeep = minCountOfSnapshotsToKeep;

        this.removeSnapshotsOlderThanDays = removeSnapshotsOlderThanDays;

        this.removeIfReleaseExists = removeIfReleaseExists;

        this.processedRepos = new HashSet<String>();

        this.deleteImmediately = deleteImmediately;
    }

    public String getRepositoryId()
    {
        return repositoryId;
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

    public void addProcessedRepo( String repoId )
    {
        this.processedRepos.add( repoId );
    }

    public boolean isProcessedRepo( String repoId )
    {
        return this.processedRepos.contains( repoId );
    }

    public boolean isDeleteImmediately()
    {
        return deleteImmediately;
    }
}
