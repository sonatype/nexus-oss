/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.model.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.upgrade.SecurityConfigurationUpgrader;

/**
 * The default configuration source powered by Modello. It will try to load configuration, upgrade if needed and
 * validate it. It also holds the one and only existing Configuration object.
 * 
 * @author cstamas
 */
@Singleton
@Typed( SecurityModelConfigurationSource.class )
@Named( "file" )
public class FileModelConfigurationSource
    extends AbstractSecurityModelConfigurationSource
{
    /**
     * The configuration file.
     */
    private File configurationFile;

    /**
     * The configuration upgrader.
     */
    private final SecurityConfigurationUpgrader configurationUpgrader;

    /**
     * The defaults configuration source.
     */
    private final SecurityModelConfigurationSource securityDefaults;

    /** Flag to mark defaulted config */
    private boolean configurationDefaulted;

    @Inject
    public FileModelConfigurationSource( @Named( "${security-xml-file}" ) File configurationFile,
                                         @Named( "static" ) SecurityModelConfigurationSource securityDefaults,
                                         SecurityConfigurationUpgrader configurationUpgrader )
    {
        this.configurationFile = configurationFile;
        this.securityDefaults = securityDefaults;
        this.configurationUpgrader = configurationUpgrader;
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
     * @deprecated replaced by constructor injection
     */
    @Deprecated
    public void setConfigurationFile( File configurationFile )
    {
        this.configurationFile = configurationFile;
    }

    public Configuration loadConfiguration()
        throws ConfigurationException, IOException
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

            // check for a configuration before saving
            // if it is null use an empty configuraiton
            if ( this.getConfiguration() == null )
            {
                Configuration configuration = new Configuration();
                configuration.setVersion( Configuration.MODEL_VERSION );
                this.setConfiguration( configuration );
            }

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

    @Override
    public SecurityModelConfigurationSource getDefaultsSource()
    {
        return securityDefaults;
    }

    protected void upgradeConfiguration( File file )
        throws IOException, ConfigurationException
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
        backupConfiguration();

        // set the upgradeInstance to warn the application about this
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
                String message =
                    "\r\n******************************************************************************\r\n"
                        + "* Could not create configuration file [ "
                        + file.toString()
                        + "]!!!! *\r\n"
                        + "* Application cannot start properly until the process has read+write permissions to this folder *\r\n"
                        + "******************************************************************************";

                getLogger().error( message );
            }

            // copy the current security config file as file.bak
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

    @Override
    public void backupConfiguration()
        throws IOException
    {
        File file = getConfigurationFile();

        File backup = new File( file.getParentFile(), file.getName() + ".bak" );

        FileUtils.copyFile( file, backup );
    }

}
