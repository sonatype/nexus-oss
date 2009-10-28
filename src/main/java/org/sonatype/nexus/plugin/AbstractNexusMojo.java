/*
 * Nexus Plugin for Maven
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.plugin;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

public abstract class AbstractNexusMojo
    extends AbstractMojo
{

    /**
     * The base URL for a Nexus Professional instance that includes the nexus-staging-plugin. If missing, the mojo will
     * prompt for this value.
     * 
     * @parameter expression="${nexusUrl}"
     */
    private String nexusUrl;

    /**
     * @component
     */
    private Prompter prompter;

    /**
     * The username that should be used to log into Nexus.
     * 
     * @parameter expression="${username}" default-value="${user.name}"
     */
    private String username;

    /**
     * If provided, lookup username/password from this server entry in the current Maven settings.
     * 
     * @parameter expression="${serverAuthId}"
     */
    private String serverAuthId;

    /**
     * The password that should be used to log into Nexus. If missing, the mojo will prompt for this value.
     * 
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * @parameter default-value="${settings}"
     * @readonly
     */
    private Settings settings;

    /**
     * <p>
     * If set to true, enable the debug log-level inside commons-httpclient (used to interact with Nexus).
     * </p>
     * <p>
     * <b>NOTE:</b> This parameter will ONLY work when used with the -X Maven switch (which enables debug logging for
     * the build).
     * </p>
     * 
     * @parameter expression="${verboseDebug}" default-value="false"
     */
    private boolean verboseDebug;

    protected AbstractNexusMojo()
    {
    }

    protected String getNexusBaseUrl()
    {
        return nexusUrl;
    }

    public String getNexusUrl()
    {
        return nexusUrl;
    }

    public void setNexusUrl( final String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
    }

    public Prompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter( final Prompter prompter )
    {
        this.prompter = prompter;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( final String username )
    {
        this.username = username;
    }

    public String getServerAuthId()
    {
        return serverAuthId;
    }

    public void setServerAuthId( final String serverAuthId )
    {
        this.serverAuthId = serverAuthId;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( final String password )
    {
        this.password = password;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void setSettings( final Settings settings )
    {
        this.settings = settings;
    }

    public boolean isVerboseDebug()
    {
        return verboseDebug;
    }

    public void setVerboseDebug( final boolean verboseDebug )
    {
        this.verboseDebug = verboseDebug;
    }

    protected abstract AbstractRESTLightClient connect()
        throws RESTLightClientException, MojoExecutionException;

    protected String formatUrl( final String url )
    {
        if ( url == null )
        {
            return null;
        }

        if ( url.length() < 1 )
        {
            return url;
        }

        return url.endsWith( "/" ) ? url.substring( 0, url.length() - 1 ) : url;
    }

    protected void initLog4j()
    {
        if ( getLog().isDebugEnabled() )
        {
            if ( isVerboseDebug() )
            {
                LogManager.getRootLogger().setLevel( Level.DEBUG );
            }
            else
            {
                LogManager.getRootLogger().setLevel( Level.INFO );
            }
        }
        else
        {
            LogManager.getRootLogger().setLevel( Level.WARN );
        }

        if ( !LogManager.getRootLogger().getAllAppenders().hasMoreElements() )
        {
            LogManager.getRootLogger().addAppender( new ConsoleAppender( new SimpleLayout() ) );
        }
    }

    protected void fillMissing()
        throws MojoExecutionException
    {
        if ( getServerAuthId() != null )
        {
            Server server = getSettings() == null ? null : getSettings().getServer( getServerAuthId() );
            if ( server != null )
            {
                getLog().info( "Using authentication information for server: '" + getServerAuthId() + "'." );

                setUsername( server.getUsername() );
                setPassword( server.getPassword() );
            }
            else
            {
                getLog().warn( "Server authentication entry not found for: '" + getServerAuthId() + "'." );
            }
        }

        while ( getPassword() == null || getPassword().trim().length() < 1 )
        {
            try
            {
                setPassword( getPrompter().promptForPassword( "Password" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

    }
    
}