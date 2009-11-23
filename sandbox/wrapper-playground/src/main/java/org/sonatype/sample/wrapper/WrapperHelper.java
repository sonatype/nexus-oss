package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

/**
 * This is a JSW Wrapper helper class to manipulate wrapper.conf. It has only one "dependency": to have set the
 * "basedir" property on System properties with path pointing to the root of the Application bundle. This is the case
 * with Nexus bundle.
 * 
 * @author cstamas
 */
public class WrapperHelper
{
    /**
     * The File pointing to basedir. Lazily inited, see getBasedir() method.
     */
    private static File basedir;

    /**
     * Backups wrapper.conf if not backed up already.
     * 
     * @throws IOException
     */
    public static void backupWrapperConf()
        throws IOException
    {
        backupWrapperConf( false );
    }

    /**
     * Backups wrapper.conf. It may be forced to do so, potentionally overwriting the backup file.
     * 
     * @param overwrite true, if you want to overwrite the backup file even if it exists.
     * @throws IOException
     */
    public static void backupWrapperConf( boolean overwrite )
        throws IOException
    {
        File wrapperConfBackup = getConfFile( "wrapper.conf.bak" );

        if ( overwrite || !wrapperConfBackup.isFile() )
        {
            FileUtils.copyFile( getWrapperConfFile(), wrapperConfBackup );
        }
    }

    /**
     * Replaces the wrapper.conf file with the provided one.
     * 
     * @param file
     * @throws IOException
     */
    public static void swapInWrapperConf( File file )
        throws IOException
    {
        FileUtils.copyFile( file, getWrapperConfFile() );
    }

    /**
     * Return the File that points to wrapper.conf.
     * 
     * @return
     */
    public static File getWrapperConfFile()
    {
        return getConfFile( "wrapper.conf" );
    }

    /**
     * Returns any configuration file from /conf directory of bundle.
     * 
     * @param name for example "wrapper.conf", but better use getWrapperConfFile() method for that!
     * @return
     */
    public static File getConfFile( String name )
    {
        return new File( getConfDir(), name );
    }

    /**
     * Returns the File pointing to /conf directory of the bundle.
     * 
     * @return
     */
    public static File getConfDir()
    {
        return new File( getBasedir(), "conf" );
    }

    /**
     * Returns the File pointing to the basedir. If it is not set in System properties, it will try to "guess" (and
     * probably give wrong results, as this method is copied from PlexusTestCase class).
     * 
     * @return
     */
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
