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

import java.util.HashMap;
import java.util.Map;

public class SnapshotRemovalResult
{
    private Map<String, SnapshotRemovalRepositoryResult> processedRepositories;

    public SnapshotRemovalResult()
    {
        super();

        this.processedRepositories = new HashMap<String, SnapshotRemovalRepositoryResult>();
    }

    public Map<String, SnapshotRemovalRepositoryResult> getProcessedRepositories()
    {
        return processedRepositories;
    }

    public void addResult( SnapshotRemovalRepositoryResult res )
    {
        if ( processedRepositories.containsKey( res.getRepositoryId() ) )
        {
            SnapshotRemovalRepositoryResult ex = processedRepositories.get( res.getRepositoryId() );

            ex.setDeletedFiles( ex.getDeletedFiles() + res.getDeletedFiles() );

            ex.setDeletedSnapshots( ex.getDeletedSnapshots() + res.getDeletedSnapshots() );
        }
        else
        {
            processedRepositories.put( res.getRepositoryId(), res );
        }
    }

}
