/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.error.reporting;

import java.io.File;

public class FileListingHelper
{
    public static final String LINE_SEPERATOR = System.getProperty("line.separator");
    
    public static String buildFileListing( File directory )
    {
        if ( !directory.exists() 
            || !directory.isDirectory() )
        {
            return "";
        }
        
        StringBuffer result = new StringBuffer();
        
        appendLine( result, "File listing for directory: " + directory.getAbsolutePath() );
        appendLine( result );
        appendLine( result, handleDirectory( directory, directory ) );
        
        return result.toString();
    }
    
    private static String handleDirectory( File root, File directory )
    {
        if ( !directory.exists() 
            || !directory.isDirectory() )
        {
            return "";
        }
        
        StringBuffer result = new StringBuffer();
        
        File[] files = directory.listFiles();
        
        for ( int i = 0 ; i < files.length ; i++ )
        {
            if ( files[i].isDirectory() )
            {
                result.append( handleDirectory( root, files[i] ) );
            }
            else
            {
                appendLine( result, getRelativePath( root.getAbsolutePath(), files[i].getAbsolutePath() ) );
            }
        }
        
        return result.toString();
    }
    
    private static String getRelativePath( String root, String path )
    {
        if ( root.equals( path ) )
        {
            return "." + File.separatorChar;
        }
        else
        {
            return path.replace( root, "." );
        }
    }
    
    private static void appendLine( StringBuffer sb, String line )
    {
        sb.append( line );
        appendLine( sb );
    }
    
    private static void appendLine( StringBuffer sb )
    {
        sb.append( LINE_SEPERATOR );
    }
}
