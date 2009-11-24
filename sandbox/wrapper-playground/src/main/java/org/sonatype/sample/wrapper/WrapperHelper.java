package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
     * The configuration to use. It is lazily instantiated, see getConfiguration() method.
     */
    private static WrapperHelperConfiguration wrapperHelperConfiguration;

    /**
     * The File pointing to basedir. Lazily inited, see getBasedir() method.
     */
    private static File basedir;

    /**
     * Gets the configuration in use.
     * 
     * @return
     */
    public static WrapperHelperConfiguration getConfiguration()
    {
        if ( wrapperHelperConfiguration == null )
        {
            wrapperHelperConfiguration = new WrapperHelperConfiguration();
        }

        return wrapperHelperConfiguration;
    }

    /**
     * Sets the configuration in use.
     * 
     * @param cfg
     */
    public static void setConfiguration( WrapperHelperConfiguration cfg )
    {
        wrapperHelperConfiguration = cfg;

        basedir = null;
    }

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
        File wrapperConfBackup = getBackupWrapperConfFile();

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
     * Replaces the wrapper.conf file with the provided one. Nice to have if configuration file comes from classpath.
     * 
     * @param url
     * @throws IOException
     */
    public static void swapInWrapperConf( URL url )
        throws IOException
    {
        FileUtils.copyURLToFile( url, getWrapperConfFile() );
    }

    /**
     * Return the File that points to wrapper.conf.
     * 
     * @return
     */
    public static File getWrapperConfFile()
    {
        return getConfFile( getConfiguration().getWrapperConfName() );
    }

    /**
     * Returns a File that points to the backup wrapper.conf. This file <b>may not exist</b>, so check is needed.
     * 
     * @return
     */
    public static File getBackupWrapperConfFile()
    {
        return getConfFile( getConfiguration().getWrapperConfBackupName() );
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
        return new File( getBasedir(), getConfiguration().getConfDirPath() );
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

        String basedirPath = System.getProperty( getConfiguration().getBasedirPropertyKey() );

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

    /**
     * Returns WrapperConfWrapper for the wrapper.conf of the bundle. It may be used with WrapperEditor for some
     * high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public static WrapperConfWrapper getWrapperConfWrapper()
        throws IOException
    {
        return getWrapperConfWrapper( getWrapperConfFile() );
    }

    /**
     * Returns WrapperConfWrapper for the backed-up wrapper.conf of the bundle. It may be used with WrapperEditor for
     * some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public static WrapperConfWrapper getBackupWrapperConfWrapper()
        throws IOException
    {
        return getWrapperConfWrapper( getBackupWrapperConfFile() );
    }

    /**
     * Returns WrapperConfWrapper for the provided file. It may be used with WrapperEditor for some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public static WrapperConfWrapper getWrapperConfWrapper( File fileToWrap )
        throws IOException
    {
        return new DefaultWrapperConfWrapper( fileToWrap );
    }

    // == private stuff

}
