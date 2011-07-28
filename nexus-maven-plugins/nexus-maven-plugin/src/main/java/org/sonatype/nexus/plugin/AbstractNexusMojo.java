/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.sonatype.nexus.plugin.discovery.NexusConnectionInfo;
import org.sonatype.nexus.plugin.discovery.NexusDiscoveryException;
import org.sonatype.nexus.plugin.discovery.NexusInstanceDiscoverer;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

public abstract class AbstractNexusMojo
    extends AbstractMojo
{

    /**
     * NOT REQUIRED IN ALL CASES. If this is available, the current project will be used in the Nexus discovery process.
     * 
     * @parameter default-value="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * If false, the Nexus discovery process will prompt the user to accept any Nexus connection information it finds
     * before using it. <br/>
     * <b>NOTE:</b> Batch-mode executions will override this parameter with an effective value of 'true'.
     * 
     * @parameter expression="${nexus.automaticDiscovery}" default-value="false"
     */
    private boolean automatic;

    /**
     * The base URL for a Nexus Professional instance that includes the nexus-staging-plugin. If missing, the mojo will
     * prompt for this value.
     * 
     * @parameter expression="${nexus.url}"
     */
    private String nexusUrl;

    /**
     * @component
     */
    private Prompter prompter;

    /**
     * The username that should be used to log into Nexus.
     * 
     * @parameter expression="${nexus.username}" default-value="${user.name}"
     */
    private String username;

    /**
     * If provided, lookup username/password from this server entry in the current Maven settings.
     * 
     * @parameter expression="${nexus.serverAuthId}"
     */
    private String serverAuthId;

    /**
     * The password that should be used to log into Nexus. If missing, the mojo will prompt for this value.
     * 
     * @parameter expression="${nexus.password}"
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
     * @parameter expression="${nexus.verboseDebug}" default-value="false"
     */
    private boolean verboseDebug;

    /**
     * @component
     */
    private NexusInstanceDiscoverer discoverer;

    protected AbstractNexusMojo()
    {
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
        if ( getNexusUrl() != null )
        {
            boolean authFound = false;
            if ( getServerAuthId() != null )
            {
                Server server = getSettings() == null ? null : getSettings().getServer( getServerAuthId() );
                if ( server != null )
                {   
                    getLog().info( "Using authentication information for server: '" + getServerAuthId() + "'." );
                    
                    try
                    {
                        setUsername( server.getUsername() );
                        setPassword( discoverer.getSecDispatcher().decrypt( server.getPassword() ) );
                        authFound = true;
                    }
                    catch ( SecDispatcherException e )
                    {
                        throw new MojoExecutionException( "Failed to decrypt Nexus password: " + e.getMessage(), e );
                    }
                }
                else
                {
                    getLog().warn( "Server authentication entry not found for: '" + getServerAuthId() + "'." );
                }
            }

            if ( !authFound && getPassword() == null )
            {
                try
                {
                    NexusConnectionInfo info =
                        discoverer.fillAuth( getNexusUrl(), getSettings(), getProject(), getUsername(),
 isAutomatic() );

                    if ( info == null )
                    {
                        throw new MojoExecutionException( "Cannot determine login credentials for Nexus instance: "
                            + getNexusUrl() );
                    }

                    setUsername( info.getUser() );
                    setPassword( discoverer.getSecDispatcher().decrypt( info.getPassword() ) );
                }
                catch ( NexusDiscoveryException e )
                {
                    throw new MojoExecutionException( "Failed to determine authentication information for Nexus at: "
                        + getNexusUrl(), e );
                }
                catch ( SecDispatcherException e )
                {
                    throw new MojoExecutionException( "Failed to decrypt Nexus password: " + e.getMessage(), e );
                }
            }
        }
        else
        {
            try
            {
                NexusConnectionInfo info =
                    discoverer.discover( getSettings(), getProject(), getUsername(), isAutomatic() );

                if ( info == null )
                {
                    throw new MojoExecutionException( "Nexus instance discovery failed." );
                }

                setNexusUrl( info.getNexusUrl() );
                setUsername( info.getUser() );
                setPassword( discoverer.getSecDispatcher().decrypt( info.getPassword() ) );
            }
            catch ( NexusDiscoveryException e )
            {
                throw new MojoExecutionException( "Failed to discover Nexus instance.", e );
            }
            catch ( SecDispatcherException e )
            {
                throw new MojoExecutionException( "Failed to decrypt Nexus password: " + e.getMessage(), e );
            }
        }
    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject( final MavenProject project )
    {
        this.project = project;
    }

    public boolean isAutomatic()
    {
        if ( getSettings() == null )
        {
            return automatic;
        }
        else
        {
            return !getSettings().isInteractiveMode() || automatic;
        }
    }

    public void setAutomatic( final boolean automatic )
    {
        this.automatic = automatic;
    }

    public NexusInstanceDiscoverer getDiscoverer()
    {
        return discoverer;
    }

    public void setDiscoverer( final NexusInstanceDiscoverer discoverer )
    {
        this.discoverer = discoverer;
    }

    public SecDispatcher getDispatcher()
    {
        return discoverer.getSecDispatcher();
    }

    public void setDispatcher( final SecDispatcher dispatcher )
    {
        this.discoverer.setSecDispatcher( dispatcher );
    }
    
}