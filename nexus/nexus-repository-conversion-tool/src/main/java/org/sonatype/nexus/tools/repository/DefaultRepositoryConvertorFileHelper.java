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
public class DefaultRepositoryConvertorFileHelper
    implements RepositoryConvertorFileHelper
{

    public void copy( File file, File target, String basePath )
        throws IOException
    {
        copyOrMove( file, target, basePath, false );
    }

    public void move( File file, File target, String basePath )
        throws IOException
    {
        copyOrMove( file, target, basePath, true );
    }

    protected void copyOrMove( File file, File target, String basePath, boolean isMove )
        throws IOException
    {
        File targetFile = new File( target, basePath + file.getName() );

        buildDirectoryPath( targetFile.getParentFile() );

        if ( file.isDirectory() )
        {
            targetFile.mkdir();

            for ( File child : file.listFiles() )
            {
                copyOrMove( child, target, basePath + file.getName() + File.separatorChar, isMove );
            }
        }
        else if ( file.isFile() )
        {
            moveFileContent( file, targetFile );
        }

        if ( isMove )
        {
            file.delete();
        }
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

    private void buildDirectoryPath( File directory )
    {
        if ( !directory.getParentFile().exists() )
        {
            buildDirectoryPath( directory.getParentFile() );
        }
        directory.mkdir();
    }

}
