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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;


/**
 * @author Juven Xu
 */
public class DefaultRepositoryConvertor
    implements RepositoryConvertor
{

    /**
     * when traverse a folder, if this make is true, then go back to the parent folder
     */
    private boolean breakToParent = false;
    
    private File currentRepository;
    
    private File releaseRepository;
    
    private File snapshotRepository;
    
    public void convertRepository( File repository, File targetPath )
    {
        currentRepository = repository;
        
        releaseRepository = new File( targetPath, currentRepository.getName() + SUFFIX_RELEASES );

        snapshotRepository = new File( targetPath, currentRepository.getName() + SUFFIX_SNAPSHOTS );

        releaseRepository.mkdir();

        snapshotRepository.mkdir();

        convert( currentRepository );
    }
    
    private void convert( File file )
    {
        if ( isRepositoryLeaf( file ) )
        {
            try{
            move( file.getParentFile() );
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            // since the whole folder containing this file is already moved
            // don't need to look the other files in this folder
            breakToParent = true;

            return;
        }
        if ( file.isDirectory() )
        {
            for ( File subFile : file.listFiles() )
            {
                if ( breakToParent )
                {
                    break;
                }
                convert( subFile );
            }
            breakToParent = false;
        }
    }

    private void move( File versionFolder ) throws IOException
    {


        if ( isSnapshot( versionFolder.getName() ) )
        {
            // move to snapshot repository
            move( versionFolder, snapshotRepository, getCoordinatePath (versionFolder) );
        }
        else{
            // move to release repository
            move( versionFolder, releaseRepository, getCoordinatePath (versionFolder) );
        }
    }
    
    private boolean isSnapshot(String version)
    {
        if (version.endsWith( "SNAPSHOT" ))
        {
            return true;
        }
        return false;
    }
    
    private boolean isRepositoryLeaf( File file )
    {
        if ( !file.isFile() )
        {
            return false;
        }
        if ( file.getName().endsWith( ".pom" ) )
        {
            return true;
        }
        if ( file.getName().endsWith( ".jar" ) )
        {
            return true;
        }
        return false;
    }
    
    
    protected void move( File file, File targetRepository,  String base )
        throws IOException
    {
        File targetFile = new File( targetRepository, base + file.getName() );

        buildDirectoryPath (targetFile.getParentFile());
        
        
        if ( file.isDirectory() )
        {
            targetFile.mkdir();

            for ( File child : file.listFiles() )
            {
                move( child, targetRepository,  base + file.getName() + File.separatorChar );
            }
        }
        else if ( file.isFile() )
        {
            moveFileContent( file, targetFile );
        }
     //   file.delete();
    }

    private void moveFileContent( File from, File to )
        throws IOException
    {
        FileInputStream fis = null;

        FileOutputStream fos = null;

        try
        {
            fis = new FileInputStream( from );

            fos = new FileOutputStream( to );

            IOUtil.copy( fis, fos );
       
            fos.flush();
        }
        catch ( IOException ioe )
        {
            throw ioe;
        }
        finally
        {
            if ( fis != null )
            {
                IOUtil.close( fis );
            }
            if ( fos != null )
            {
                IOUtil.close( fos );
            }
        }

    }
    
    private void buildDirectoryPath( File directory)
    {
        if (!directory.getParentFile().exists())
        {
            buildDirectoryPath(directory.getParentFile());
        }
        directory.mkdir();
    }
    
    private String getCoordinatePath( File versionFolder )
    {
        String temp = versionFolder.getAbsolutePath().substring( currentRepository.getAbsolutePath().length() );
        return temp.substring( 0, temp.length() - versionFolder.getName().length() );
    }
}
