/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.application.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.upgrade.ApplicationConfigurationUpgrader;
import org.sonatype.nexus.configuration.application.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.source.ConfigurationSource;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 * 
 * @author cstamas
 * @plexus.component role-hint="file"
 */
public class FileConfigurationSource
    extends AbstractApplicationConfigurationSource
{

    /**
     * The configuration file.
     * 
     * @plexus.configuration default-value="${nexus.configuration}"
     */
    private File configurationFile;

    /**
     * The configuration validator.
     * 
     * @plexus.requirement
     */
    private ApplicationConfigurationValidator configurationValidator;

    /**
     * The configuration upgrader.
     * 
     * @plexus.requirement
     */
    private ApplicationConfigurationUpgrader configurationUpgrader;

    /**
     * The nexus defaults configuration source.
     * 
     * @plexus.requirement role-hint="static"
     */
    private ApplicationConfigurationSource nexusDefaults;

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
            throw new IllegalArgumentException( "ConfigurationValidator is invalid type " + configurationValidator.getClass().getName() );
        }
        
        this.configurationValidator = ( ApplicationConfigurationValidator ) configurationValidator;
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

        loadConfiguration( getConfigurationFile() );

        // check for loaded model
        if ( getConfiguration() == null )
        {
            upgradeConfiguration( getConfigurationFile() );

            loadConfiguration( getConfigurationFile() );
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
    @SuppressWarnings( "unchecked" )
    private void loadConfiguration( File file )
        throws IOException
    {
        getLogger().info( "Loading Nexus configuration from " + file.getAbsolutePath() );

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

        try
        {
            file.getParentFile().mkdirs();

            fos = new FileOutputStream( file );

            saveConfiguration( fos, getConfiguration() );

            fos.flush();

            fos.close();
        }
        finally
        {
            if ( fos != null )
            {
                fos.flush();

                fos.close();
            }
        }
    }

    /**
     * Was the active configuration fetched from config file or from default source? True if it from default source.
     */
    public boolean isConfigurationDefaulted()
    {
        return configurationDefaulted;
    }

}
