/**
 * 
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileFilter;

public class MavenProjectFileFilter
    implements FileFilter
{

    public static final MavenProjectFileFilter INSTANCE = new MavenProjectFileFilter();

    private MavenProjectFileFilter()
    {
        super();
    }

    public boolean accept( File pathname )
    {
        return ( !pathname.getName().endsWith( ".svn" ) && pathname.isDirectory() && new File( pathname, "pom.xml" ).exists() );
    }
}