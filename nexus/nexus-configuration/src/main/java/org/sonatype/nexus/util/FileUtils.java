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
package org.sonatype.nexus.util;

import java.io.File;

/**
 * Some utils that should end in plexus-utils.
 * 
 * @author cstamas
 */
public class FileUtils
{

    /**
     * Recursively count files in a directory.
     * 
     * @return count of files in directory.
     */
    public static long filesInDirectory( final String directory )
        throws IllegalArgumentException
    {
        return filesInDirectory( new File( directory ) );
    }

    /**
     * Recursively count files in a directory.
     * 
     * @return count of files in directory.
     */
    public static long filesInDirectory( final File directory )
        throws IllegalArgumentException
    {
        if ( !directory.exists() )
        {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException( message );
        }

        if ( !directory.isDirectory() )
        {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException( message );
        }

        long count = 0;

        final File[] files = directory.listFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            final File file = files[i];

            if ( file.isDirectory() )
            {
                count += filesInDirectory( file );
            }
            else
            {
                count++;
            }
        }

        return count;
    }
}
