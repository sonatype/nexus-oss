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

public class SnapshotRemovalRepositoryResult
{
    private final String repoId;

    private int deletedSnapshots;

    private int deletedFiles;

    public SnapshotRemovalRepositoryResult( String repoId, int deletedSnapshots, int deletedFiles )
    {
        this.repoId = repoId;

        this.deletedSnapshots = deletedSnapshots;

        this.deletedFiles = deletedFiles;
    }

    public String getRepositoryId()
    {
        return repoId;
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

}
