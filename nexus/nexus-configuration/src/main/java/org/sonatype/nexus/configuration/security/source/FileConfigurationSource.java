/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.security.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.upgrade.SecurityConfigurationUpgrader;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 * 
 * @author cstamas
 */
@Component( role = SecurityConfigurationSource.class, hint = "file" )
public class FileConfigurationSource
    extends AbstractSecurityConfigurationSource
{

    /**
     * The configuration file.
     */
    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}/conf/security.xml" )
    private File configurationFile;

    /**
     * The configuration upgrader.
     */
    @Requirement
    private SecurityConfigurationUpgrader configurationUpgrader;

    /**
     * The nexus defaults configuration source.
     */
    @Requirement( hint = "static" )
    private SecurityConfigurationSource securityDefaults;

    /** Flag to mark defaulted config */
    private boolean configurationDefaulted;

    /**
     * Gets the configuration file.
     * 
     * @return the configuration file
     */
    public File getConfigurationFile()
    {
        return configurationFile;
    }

    /**
     * Sets the configuration file.
     * 
     * @param configurationFile the new configuration file
     */
    public void setConfigurationFile( File configurationFile )
    {
        this.configurationFile = configurationFile;
    }

    public Configuration loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        // propagate call and fill in defaults too
        securityDefaults.loadConfiguration();

        if ( getConfigurationFile() == null || getConfigurationFile().getAbsolutePath().contains( "${" ) )
        {
            throw new ConfigurationException( "The configuration file is not set or resolved properly: "
                + ( getConfigurationFile() == null ? "null" : getConfigurationFile().getAbsolutePath() ) );
        }

        if ( !getConfigurationFile().exists() )
        {
            getLogger().warn( "No configuration file in place, copying the default one and continuing with it." );

            // get the defaults and stick it to place
            setConfiguration( securityDefaults.getConfiguration() );

            saveConfiguration( getConfigurationFile() );

            configurationDefaulted = true;
        }
        else
        {
            configurationDefaulted = false;
        }

        loadConfiguration( getConfigurationFile() );

        // check for loaded model
        if ( getConfiguration() == null )
        {
            upgradeConfiguration( getConfigurationFile() );

            loadConfiguration( getConfigurationFile() );
        }

        return getConfiguration();
    }

    public void storeConfiguration()
        throws IOException
    {
        saveConfiguration( getConfigurationFile() );
    }

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return new FileInputStream( getConfigurationFile() );
    }

    public SecurityConfigurationSource getDefaultsSource()
    {
        return securityDefaults;
    }

    protected void upgradeConfiguration( File file )
        throws IOException,
            ConfigurationException
    {
        getLogger().info( "Trying to upgrade the configuration file " + file.getAbsolutePath() );

        setConfiguration( configurationUpgrader.loadOldConfiguration( file ) );

        // after all we should have a configuration
        if ( getConfiguration() == null )
        {
            throw new ConfigurationException( "Could not upgrade Security configuration! Please replace the "
                + file.getAbsolutePath() + " file with a valid Security configuration file." );
        }

        getLogger().info( "Creating backup from the old file and saving the upgraded configuration." );

        // backup the file
        File backup = new File( file.getParentFile(), file.getName() + ".bak" );

        FileUtils.copyFile( file, backup );

        // set the upgradeInstance to warn Nexus about this
        setConfigurationUpgraded( true );

        saveConfiguration( file );
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void loadConfiguration( File file )
        throws IOException
    {
        getLogger().info( "Loading Security configuration from " + file.getAbsolutePath() );

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );

            loadConfiguration( fis );
        }
        finally
        {
            if ( fis != null )
            {
                fis.close();
            }
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void saveConfiguration( File file )
        throws IOException
    {
        FileOutputStream fos = null;

        File backupFile = new File( file.getParentFile(), file.getName() + ".old" );

        try
        {
            // Create the dir if doesn't exist, throw runtime exception on failure
            // bad bad bad
            if ( !file.getParentFile().exists() && !file.getParentFile().mkdirs() )
            {
                String message = "\r\n******************************************************************************\r\n"
                    + "* Could not create configuration file [ "
                    + file.toString()
                    + "]!!!! *\r\n"
                    + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
                    + "******************************************************************************";

                getLogger().fatalError( message );
            }

            // copy the current nexus config file as file.bak
            if ( file.exists() )
            {
                FileUtils.copyFile( file, backupFile );
            }

            fos = new FileOutputStream( file );

            saveConfiguration( fos, getConfiguration() );

            fos.flush();
        }
        finally
        {
            IOUtil.close( fos );
        }

        // if all went well, delete the bak file
        backupFile.delete();
    }

    /**
     * Was the active configuration fetched from config file or from default source? True if it from default source.
     */
    public boolean isConfigurationDefaulted()
    {
        return configurationDefaulted;
    }

}
