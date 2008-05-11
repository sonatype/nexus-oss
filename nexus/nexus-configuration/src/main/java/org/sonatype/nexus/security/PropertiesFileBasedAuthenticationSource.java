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
 * @plexus.component role="org.sonatype.nexus.security.AuthenticationSource" instantiation-strategy="per-lookup"
 *                   role-hint="properties"
 */
public class PropertiesFileBasedAuthenticationSource
    extends AbstractLogEnabled
    implements MutableAuthenticationSource, Initializable, ConfigurationChangeListener
{
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
                    saveSecrets();
                }
                fis = new FileInputStream( secretFile );

                secrets.load( fis );
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

                secrets.store( fos, "Created by Sonatype Nexus" );
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

    public boolean isKnown( String username )
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

            return secrets.containsKey( username );
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not load the secrets!", e );

            return false;
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

        if ( secret == null )
        {
            throw new NullPointerException(
                "The secret/password cannot be 'null' (Use unsetPassword() to unset secrets!)!" );
        }

        if ( secrets == null )
        {
            loadSecrets();
        }

        secrets.put( username, StringDigester.getSha1Digest( secret ) );

        saveSecrets();
    }

    public void unsetPassword( String username )
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

        secrets.put( username, "" );

        saveSecrets();
    }

    public boolean isAnynonymousAllowed()
    {
        return applicationConfiguration.getConfiguration().getSecurity().isAnonymousAccessEnabled();
    }

}
