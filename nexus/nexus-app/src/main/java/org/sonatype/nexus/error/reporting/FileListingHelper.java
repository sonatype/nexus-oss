package org.sonatype.nexus.error.reporting;

import java.io.File;

public class FileListingHelper
{
    private static final String LINE_SEPERATOR = System.getProperty("line.separator");
    
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
        
        appendLine( result, getRelativePath( root.getAbsolutePath(), directory.getAbsolutePath() ) );
        
        File[] files = directory.listFiles();
        
        for ( int i = 0 ; i < files.length ; i++ )
        {
            if ( files[i].isDirectory() )
            {
                appendLine( result, handleDirectory( root, files[i] ) );
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
