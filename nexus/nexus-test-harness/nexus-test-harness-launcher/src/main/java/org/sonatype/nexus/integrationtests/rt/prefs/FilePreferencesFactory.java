package org.sonatype.nexus.integrationtests.rt.prefs;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreferencesFactory implementation that stores the preferences in a user-defined file. To use it, set the system
 * property <tt>java.util.prefs.PreferencesFactory</tt> to <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 * <p/>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the system property
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>. Modified by cstamas, switched to SLF4J logging, and
 * exposed preferences file property.
 * 
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferencesFactory.java 282 2009-06-18 17:05:18Z david $
 */
public class FilePreferencesFactory
    implements PreferencesFactory
{
    private static final Logger log = LoggerFactory.getLogger( FilePreferencesFactory.class.getName() );

    Preferences rootPreferences;

    public static final String SYSTEM_PROPERTY_FILE = "net.infotrek.util.prefs.FilePreferencesFactory.file";

    public Preferences systemRoot()
    {
        return userRoot();
    }

    public Preferences userRoot()
    {
        if ( rootPreferences == null )
        {
            log.debug( "Instantiating root preferences" );

            rootPreferences = new FilePreferences( null, "" );
        }

        return rootPreferences;
    }

    private static File preferencesFile;

    public static File getPreferencesFile()
    {
        if ( preferencesFile == null )
        {
            String prefsFile = System.getProperty( SYSTEM_PROPERTY_FILE );

            if ( prefsFile == null || prefsFile.length() == 0 )
            {
                prefsFile = System.getProperty( "user.home" ) + File.separator + ".fileprefs";
            }

            preferencesFile = new File( prefsFile ).getAbsoluteFile();

            log.info( "Preferences file is {}", preferencesFile );
        }

        return preferencesFile;
    }

    public static void setPreferencesFile( File file )
    {
        preferencesFile = file;
    }
}