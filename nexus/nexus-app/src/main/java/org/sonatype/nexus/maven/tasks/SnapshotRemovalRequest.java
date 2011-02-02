/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

    private final Set<String> metadataRebuildPaths;
    
    private final Set<String> processedRepos;

    public SnapshotRemovalRequest( String repositoryId, int minCountOfSnapshotsToKeep,
        int removeSnapshotsOlderThanDays, boolean removeIfReleaseExists )
    {
        super();

        this.repositoryId = repositoryId;

        this.minCountOfSnapshotsToKeep = minCountOfSnapshotsToKeep;

        this.removeSnapshotsOlderThanDays = removeSnapshotsOlderThanDays;

        this.removeIfReleaseExists = removeIfReleaseExists;

        this.metadataRebuildPaths = new HashSet<String>();
        
        this.processedRepos = new HashSet<String>();
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

    public Set<String> getMetadataRebuildPaths()
    {
        return metadataRebuildPaths;
    }
    
    public void addProcessedRepo( String repoId )
    {
        this.processedRepos.add( repoId );
    }
    
    public boolean isProcessedRepo( String repoId )
    {
        return this.processedRepos.contains( repoId );
    }
}
