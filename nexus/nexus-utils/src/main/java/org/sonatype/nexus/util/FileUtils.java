/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Some utils that should end in plexus-utils.
 * 
 * @author cstamas
 */
public class FileUtils
{
    private static Set<File> roots = null;
    
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
    
    public static boolean validFileUrl( String url )
    {
        boolean result = true;
        
        if ( !validFile( new File( url ) ) )
        {
            //Failed w/ straight file, now time to try URL
            try
            {                
                if ( !validFile( new File( new URL( url ).getFile() ) ) )
                {
                    result = false;    
                }
            }
            catch ( MalformedURLException e )
            {
                result = false;
            }
        }
        
        return result;
    }
    
    public static boolean validFile( File file )
    {
        if ( roots == null )
        {
            roots = new HashSet<File>();
            
            File[] listedRoots = File.listRoots();
            
            for ( int i = 0 ; i < listedRoots.length ; i++ )
            {
                roots.add( listedRoots[i] );
            }   
            
            // Allow UNC based paths on windows
            // i.e. \\someserver\repository\central\blah
            if ( isWindows() )
            {
                roots.add( new File("\\\\") );
            }
        }
        
        File root = file;
        
        while ( root.getParentFile() != null )
        {
            root = root.getParentFile();
        }
        
        return roots.contains( root );
    }
    
    public static boolean isWindows()
    {
        return System.getProperty( "os.name" ).indexOf( "Windows" ) != -1;
    }
}
