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

package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * 
 * @author Juven Xu
 *
 */
@Component( role = ConvertorCommand.class, hint = RepositorySeperationConvertorCommand.ID )
public class RepositorySeperationConvertorCommand
    implements ConvertorCommand
{
 
    public static final String ID = "RepositorySeperationConvertorCommand";
    
    @Requirement
    private RepositoryConvertorFileHelper repositoryConvertorFileHelper;

    private boolean isMove = false;

    private File repository;

    private File releaseRepository;

    private File snapshotRepository;


    public void setMove( boolean isMove )
    {
        this.isMove = isMove;
    }

    public void setRepository( File repository )
    {
        this.repository = repository;
    }

    public void setReleaseRepository( File releaseRepository )
    {
        this.releaseRepository = releaseRepository;
    }

    public void setSnapshotRepository( File snapshotRepository )
    {
        this.snapshotRepository = snapshotRepository;
    }

    public void execute( List<File> artifactVersions )
        throws IOException
    {
        for ( File version : artifactVersions )
        {
            if ( isMove )
            {
                moveToTargetRepository( version );
            }
            else
            {
                copyToTargetRepository( version );
            }
        }
    }

    /**
     * copy the version folder to its target repository according to it's version type
     * 
     * @param versionFolder A folder in repository, normally its name is a version of an artifact
     * @throws IOException
     */
    private void copyToTargetRepository( File versionFolder )
        throws IOException
    {
        repositoryConvertorFileHelper.copy(
            versionFolder,
            getTargetRepository( versionFolder ),
            getCoordinatePath( versionFolder ) );
    }

    /**
     * move the version folder to its target repository according to it's version type
     * 
     * @param versionFolder A folder in repository, normally its name is a version of an artifact
     * @throws IOException
     */
    private void moveToTargetRepository( File versionFolder )
        throws IOException
    {
        repositoryConvertorFileHelper.move(
            versionFolder,
            getTargetRepository( versionFolder ),
            getCoordinatePath( versionFolder ) );
    }

    private File getTargetRepository( File versionFolder )
    {
        if ( isSnapshot( versionFolder.getName() ) )
        {
            return snapshotRepository;
        }
        else
        {
            return releaseRepository;
        }
    }

    /**
     * @param versionFolder
     * @return The path from repository root to the parent of our version folder
     */
    private String getCoordinatePath( File versionFolder )
    {
        String temp = versionFolder.getAbsolutePath().substring( repository.getAbsolutePath().length() );

        return temp.substring( 0, temp.length() - versionFolder.getName().length() );
    }

    private boolean isSnapshot( String version )
    {
        if ( version.endsWith( "SNAPSHOT" ) )
        {
            return true;
        }
        return false;
    }

}
