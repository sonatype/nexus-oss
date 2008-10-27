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
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Juven Xu
 */
@Component( role = RepositoryConvertor.class )
public class DefaultRepositoryConvertor
    implements RepositoryConvertor
{

    public static final String VERSION_REGEX = "^[0-9].*$";
    

    @Requirement( role = ConvertorCommand.class , hint = RepositorySeperationConvertorCommand.ID )
    private RepositorySeperationConvertorCommand repositorySeperationConvertorCommand; 
    
    private File currentRepository;

    private File releaseRepository;

    private File snapshotRepository;
    
    private List<ConvertorCommand> convertorCommands = new ArrayList<ConvertorCommand>();

    
    public void convertRepositoryWithCopy( File repository, File targetPath )
        throws IOException
    {
        setUp( repository, targetPath );
        
        convertorCommands.clear();
        
        repositorySeperationConvertorCommand.setRepository( currentRepository );
        
        repositorySeperationConvertorCommand.setReleaseRepository( releaseRepository );
        
        repositorySeperationConvertorCommand.setSnapshotRepository( snapshotRepository );
        
        repositorySeperationConvertorCommand.setMove( false );

        convertorCommands.add( repositorySeperationConvertorCommand );
        
        iterate( currentRepository, false );
    }

    public void convertRepositoryWithMove( File repository, File targetPath )
        throws IOException
    {
        setUp( repository, targetPath );
        
        convertorCommands.clear();
        
        repositorySeperationConvertorCommand.setRepository( currentRepository );
        
        repositorySeperationConvertorCommand.setReleaseRepository( releaseRepository );
        
        repositorySeperationConvertorCommand.setSnapshotRepository( snapshotRepository );
        
        repositorySeperationConvertorCommand.setMove( true );

        convertorCommands.add( repositorySeperationConvertorCommand );
        
        iterate( currentRepository, true );
        
        deleteCurrentRepository();
    }

    private void setUp( File repository, File targetPath )
    {
        currentRepository = repository;

        releaseRepository = new File( targetPath, currentRepository.getName() + SUFFIX_RELEASES );

        snapshotRepository = new File( targetPath, currentRepository.getName() + SUFFIX_SNAPSHOTS );

        releaseRepository.mkdir();

        snapshotRepository.mkdir();
    }

    
    private void iterate( File file , boolean isMove)
        throws IOException
    {
        List<File> artifactVersions = getArtifactVersions( file );

        if ( !artifactVersions.isEmpty() )
        {
            for (ConvertorCommand convertorCommand : convertorCommands )
            {
                convertorCommand.execute( artifactVersions );
            }
        }
        else if ( file.isDirectory() )
        {
            for ( File subFile : file.listFiles() )
            {
                iterate( subFile , isMove);
            }
        }
    }
    
    /**
     * Check if the file is the artifact directory, which contains sub-directory named by version
     * 
     * @param file
     * @return
     */
    private List<File> getArtifactVersions( File file )
    {
        List<File> artifactVersions = new ArrayList<File>();
        
        if ( !file.exists() || !file.isDirectory() )
        {
            return artifactVersions;
        }
        else
        {
            for ( File subFile : file.listFiles() )
            {
                if ( subFile.getName().matches( VERSION_REGEX ))
                {
                    artifactVersions.add( subFile );
                }
            }
            return artifactVersions;
        }
    }
    
    private void deleteCurrentRepository()
    {
        deleteFile (currentRepository);
    }
    
    private void deleteFile( File file )
    {
        if ( file.isDirectory() )
        {
            for ( File subFile : file.listFiles() )
            {
                deleteFile( subFile );
            }
        }
        file.delete();
    }

}
