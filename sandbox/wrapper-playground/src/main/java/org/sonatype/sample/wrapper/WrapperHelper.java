package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

public class WrapperHelper
{
    private static File basedir;

    public static void backupWrapperConf()
        throws IOException
    {
        backupWrapperConf( false );
    }

    public static void backupWrapperConf( boolean overwrite )
        throws IOException
    {
        File wrapperConfBackup = getConfFile( "wrapper.conf.bak" );

        if ( overwrite || !wrapperConfBackup.isFile() )
        {
            FileUtils.copyFile( getWrapperConfFile(), wrapperConfBackup );
        }
    }

    public static void swapInWrapperConf( File file )
        throws IOException
    {
        FileUtils.copyFile( file, getWrapperConfFile() );
    }

    public static File getWrapperConfFile()
    {
        return getConfFile( "wrapper.conf" );
    }

    public static File getConfFile( String name )
    {
        return new File( getConfDir(), name );
    }

    public static File getConfDir()
    {
        return new File( getBasedir(), "conf" );
    }

    public static File getBasedir()
    {
        if ( basedir != null )
        {
            return basedir;
        }

        String basedirPath = System.getProperty( "basedir" );

        if ( basedirPath == null )
        {
            basedir = new File( "" ).getAbsoluteFile();
        }
        else
        {
            basedir = new File( basedirPath ).getAbsoluteFile();
        }

        return basedir;
    }

}
