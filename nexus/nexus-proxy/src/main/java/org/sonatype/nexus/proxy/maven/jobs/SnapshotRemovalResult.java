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
package org.sonatype.nexus.proxy.maven.jobs;

import java.util.ArrayList;
import java.util.List;

public class SnapshotRemovalResult
{
    private List<String> processedRepositories;

    private int deletedSnapshots;

    private int deletedFiles;

    public SnapshotRemovalResult()
    {
        super();

        this.deletedSnapshots = 0;

        this.deletedFiles = 0;
    }

    public SnapshotRemovalResult( String repoId, int deletedSnapshots, int deletedFiles )
    {
        this();

        getProcessedRepositories().add( repoId );

        this.deletedSnapshots = deletedSnapshots;

        this.deletedFiles = deletedFiles;
    }

    public int getDeletedSnapshots()
    {
        return deletedSnapshots;
    }

    public void setDeletedSnapshots( int deletedSnapshots )
    {
        this.deletedSnapshots = deletedSnapshots;
    }

    public int getDeletedFiles()
    {
        return deletedFiles;
    }

    public void setDeletedFiles( int deletedFiles )
    {
        this.deletedFiles = deletedFiles;
    }

    public List<String> getProcessedRepositories()
    {
        if ( processedRepositories == null )
        {
            this.processedRepositories = new ArrayList<String>();
        }
        return processedRepositories;
    }

    public void append( SnapshotRemovalResult res )
    {
        getProcessedRepositories().addAll( res.getProcessedRepositories() );

        setDeletedFiles( getDeletedFiles() + res.getDeletedFiles() );

        setDeletedSnapshots( getDeletedSnapshots() + res.getDeletedSnapshots() );
    }

}
