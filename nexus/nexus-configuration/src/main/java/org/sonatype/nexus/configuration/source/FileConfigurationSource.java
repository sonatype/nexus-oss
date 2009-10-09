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
package org.sonatype.nexus.configuration.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.upgrade.ApplicationConfigurationUpgrader;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 * 
 * @author cstamas
 */
@Component( role = ApplicationConfigurationSource.class, hint = "file" )
public class FileConfigurationSource
    extends AbstractApplicationConfigurationSource
{

    /**
     * The configuration file.
     */
    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}/conf/nexus.xml" )
    private File configurationFile;

    /**
     * The configuration validator.
     */
    @Requirement
    private ApplicationConfigurationValidator configurationValidator;

    /**
     * The configuration upgrader.
     */
    @Requirement
    private ApplicationConfigurationUpgrader configurationUpgrader;

    /**
     * The nexus defaults configuration source.
     */
    @Requirement( hint = "static" )
    private ApplicationConfigurationSource nexusDefaults;

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;
    
    @Requirement
    private ConfigurationHelper configHelper;

    /** Flag to mark defaulted config */
    private boolean configurationDefaulted;
    
    /**
     * Gets the configuration validator.
     * 
     * @return the configuration validator
     */
    public ConfigurationValidator getConfigurationValidator()
    {
        return configurationValidator;
    }

    /**
     * Sets the configuration validator.
     * 
     * @param configurationValidator the new configuration validator
     */
    public void setConfigurationValidator( ConfigurationValidator configurationValidator )
    {
        if ( !ApplicationConfigurationValidator.class.isAssignableFrom( configurationValidator.getClass() ) )
        {
            throw new IllegalArgumentException( "ConfigurationValidator is invalid type "
                + configurationValidator.getClass().getName() );
        }

        this.configurationValidator = (ApplicationConfigurationValidator) configurationValidator;
    }

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
        nexusDefaults.loadConfiguration();

        if ( getConfigurationFile() == null || getConfigurationFile().getAbsolutePath().contains( "${" ) )
        {
            throw new ConfigurationException( "The configuration file is not set or resolved properly: "
                + getConfigurationFile().getAbsolutePath() );
        }

        if ( !getConfigurationFile().exists() )
        {
            getLogger().warn( "No configuration file in place, copying the default one and continuing with it." );

            // get the defaults and stick it to place
            setConfiguration( nexusDefaults.getConfiguration() );

            saveConfiguration( getConfigurationFile() );

            configurationDefaulted = true;
        }
        else
        {
            configurationDefaulted = false;
        }

        try
        {
        loadConfiguration( getConfigurationFile() );
        }
        catch ( ConfigurationException e )
        {
            getLogger().info( "Configuration file is invalid, attempting upgrade" );

            upgradeConfiguration( getConfigurationFile() );

            loadConfiguration( getConfigurationFile() );
            
            // if the configuration is upgraded we need to reload the security.
            // it would be great if this was put somewhere else, but I am out of ideas.
            // the problem is the default security was already loaded with the security-system component was loaded
            // so it has the defaults, the upgrade from 1.0.8 -> 1.4 moves security out of the nexus.xml
            // and we cannot use the 'correct' way of updating the info, because that would cause an infinit loop loading the nexus.xml
            this.eventMulticaster.notifyEventListeners( new SecurityConfigurationChangedEvent( null ) );
        }

        ValidationResponse vResponse = getConfigurationValidator().validateModel(
            new ValidationRequest( getConfiguration() ) );

        setValidationResponse( vResponse );

        if ( vResponse.isValid() )
        {
            if ( vResponse.isModified() )
            {
                getLogger().info( "Validation has modified the configuration, storing the changes." );

                storeConfiguration();
            }

            return getConfiguration();
        }
        else
        {
            throw new InvalidConfigurationException( vResponse );
        }
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

    public ApplicationConfigurationSource getDefaultsSource()
    {
        return nexusDefaults;
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
            throw new ConfigurationException( "Could not upgrade Nexus configuration! Please replace the "
                + file.getAbsolutePath() + " file with a valid Nexus configuration file." );
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
        throws IOException,
            ConfigurationException
    {
        getLogger().info( "Loading Nexus configuration from " + file.getAbsolutePath() );

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );

            loadConfiguration( fis );
            
            // seems a bit dirty, but the config might need to be upgraded.
            if( this.getConfiguration() != null )
            {
                // decrypt the passwords
                configHelper.encryptDecryptPasswords( this.getConfiguration(), false );
            }
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

            // Clone the conf so we can encrypt the passwords
            Configuration copyOfConfig = configHelper.clone( this.getConfiguration() );

            // encrypt the passwords
            configHelper.encryptDecryptPasswords( copyOfConfig, true );

            fos = new FileOutputStream( file );

            saveConfiguration( fos, copyOfConfig );

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