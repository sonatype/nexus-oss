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
package org.sonatype.nexus.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.util.StringDigester;

/**
 * The Class SimpleAuthenticationSource that uses simple props file for storing username pwds.
 * 
 * @author cstamas
 * @plexus.component role-hint="simple"
 */
public class SimpleAuthenticationSource
    extends AbstractLogEnabled
    implements AuthenticationSource, Initializable, ConfigurationChangeListener
{
    public static final String ADMIN_USERNAME = "admin";

    public static final String DEPLOYMENT_USERNAME = "deployment";

    /**
     * @plexus.requirement
     */
    protected ApplicationConfiguration applicationConfiguration;

    protected Properties secrets;

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addConfigurationChangeListener( this );

        try
        {
            // check for old nexus pwd
            File oldSecretFile = new File( applicationConfiguration.getConfigurationDirectory(), "secret.txt" );

            if ( oldSecretFile.exists() )
            {
                getLogger().info( "Found pre-beta3 secret.txt file with 'admin' password, trying to upgrade it." );

                String oldAdminHash = FileUtils.fileRead( oldSecretFile ).trim();

                loadSecrets();

                secrets.put( ADMIN_USERNAME, oldAdminHash );

                saveSecrets();

                oldSecretFile.delete();
            }

            loadSecrets();
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Error loading secret.properties!", e );
        }
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        secrets = null;
    }

    protected File getSecretsFile()
    {
        return new File( applicationConfiguration.getConfigurationDirectory(), "secret.properties" );
    }

    protected void loadSecrets()
        throws IOException
    {
        synchronized ( this )
        {
            secrets = new Properties();

            FileInputStream fis = null;

            try
            {
                File secretFile = getSecretsFile();

                if ( !secretFile.exists() )
                {
                    secrets.put( ADMIN_USERNAME, StringDigester.getSha1Digest( "admin123" ) );

                    secrets.put( DEPLOYMENT_USERNAME, "" );

                    saveSecrets();
                }
                fis = new FileInputStream( secretFile );

                secrets.loadFromXML( fis );
            }
            finally
            {
                IOUtil.close( fis );
            }
        }
    }

    protected void saveSecrets()
        throws IOException
    {
        synchronized ( this )
        {
            FileOutputStream fos = null;

            try
            {
                File secretFile = getSecretsFile();

                fos = new FileOutputStream( secretFile );

                secrets.storeToXML( fos, "Created by Sonatype Nexus" );
            }
            finally
            {
                IOUtil.close( fos );
            }
        }
    }

    public User authenticate( String username, String password )
    {
        if ( username == null )
        {
            throw new NullPointerException( "The username cannot be 'null'!" );
        }

        if ( secrets == null )
        {
            try
            {
                loadSecrets();
            }
            catch ( IOException e )
            {
                getLogger().error( "Could not load the secrets!", e );

                return null;
            }
        }

        if ( ADMIN_USERNAME.equals( username ) || DEPLOYMENT_USERNAME.equals( username ) )
        {
            if ( !StringUtils.isEmpty( secrets.getProperty( username ) ) )
            {
                getLogger().debug( "Trying " + username + " authentication..." );

                String inputHash = StringDigester.getSha1Digest( password );

                String neededHash = secrets.getProperty( username );

                if ( inputHash != null && neededHash != null && inputHash.equals( neededHash ) )
                {
                    return new SimpleUser( username );
                }
                else
                {
                    return null;
                }
            }
            else
            {
                getLogger().debug( "User " + username + " has no password set, passing it as authenticated..." );

                return new SimpleUser( username );
            }
        }
        else
        {
            return null;
        }
    }

    public boolean hasPasswordSet( String username )
    {
        try
        {
            if ( username == null )
            {
                throw new NullPointerException( "The username cannot be 'null'!" );
            }

            if ( secrets == null )
            {
                loadSecrets();
            }

            return !StringUtils.isEmpty( secrets.getProperty( username ) );
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not load the secrets!", e );

            return false;
        }
    }

    public void setPassword( String username, String secret )
        throws IOException
    {
        if ( username == null )
        {
            throw new NullPointerException( "The username cannot be 'null'!" );
        }

        if ( secrets == null )
        {
            loadSecrets();
        }

        if ( !StringUtils.isEmpty( secret ) )
        {
            secrets.put( username, StringDigester.getSha1Digest( secret ) );
        }
        else
        {
            secrets.put( username, "" );
        }

        saveSecrets();
    }
}
