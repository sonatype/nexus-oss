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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class AbstractStagingMojo
    extends AbstractNexusMojo
{

    private StageClient client;

    public AbstractStagingMojo()
    {
        super();
    }

    @Override
    protected synchronized StageClient connect()
        throws RESTLightClientException
    {
        String url = formatUrl( getNexusBaseUrl() );

        getLog().info( "Logging into Nexus: " + url );
        getLog().info( "User: " + getUsername() );

        client = new StageClient( url, getUsername(), getPassword() );
        return client;
    }

    protected StageClient getClient()
        throws MojoExecutionException
    {
        if ( client == null )
        {
            try
            {
                connect();
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to connect to Nexus at: " + getNexusUrl() );
            }
        }

        return client;
    }

    protected void listRepos( final String groupId, final String artifactId, final String version,
                                    final String prompt )
        throws MojoExecutionException
    {
        List<StageRepository> repos;
        StringBuilder builder = new StringBuilder();

        try
        {
            if ( groupId != null )
            {
                repos = getClient().getClosedStageRepositoriesForUser( groupId, artifactId, version );
                builder.append( prompt )
                       .append( " for: '" )
                       .append( groupId )
                       .append( ":" )
                       .append( artifactId )
                       .append( ":" )
                       .append( version )
                       .append( "':" );
            }
            else
            {
                repos = getClient().getClosedStageRepositoriesForUser();
                builder.append( prompt ).append( ": " );
            }
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to list staging repositories: " + e.getMessage(), e );
        }

        if ( repos.isEmpty() )
        {
            builder.append( "\n\nNone." );
        }
        else
        {
            for ( StageRepository repo : repos )
            {
                builder.append( "\n\n-  " );
                builder.append( listRepo( repo ) );
            }
        }

        builder.append( "\n\n" );

        getLog().info( builder.toString() );
    }

    protected CharSequence listRepo( final StageRepository repo )
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append( repo.getRepositoryId() )
               .append( " (profile: " )
               .append( repo.getProfileName() )
               .append( ")" );

        if ( repo.getUrl() != null )
        {
            builder.append( "\n   URL: " ).append( repo.getUrl() );
        }

        if ( repo.getDescription() != null )
        {
            builder.append( "\n   Description: " ).append( repo.getDescription() );
        }
        
        return builder;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillMissing()
        throws MojoExecutionException
    {
        super.fillMissing();

        if ( getNexusBaseUrl() == null && getServerAuthId() != null && getSettings() != null )
        {
            List<Mirror> mirrors = getSettings().getMirrors();
            if ( mirrors != null && !mirrors.isEmpty() )
            {
                for ( Mirror mirror : mirrors )
                {
                    if ( mirror.getId().equals( getServerAuthId() ) )
                    {
                        setNexusUrl( mirror.getUrl() );
                        
                        try
                        {
                            connect();
                        }
                        catch ( RESTLightClientException e )
                        {
                            if ( getLog().isDebugEnabled() )
                            {
                                getLog().debug( "Failed to connect using URL: " + getNexusUrl(), e );
                            }
                            else
                            {
                                getLog().info( "Failed to connect using URL: " + getNexusUrl() );
                            }

                            setNexusUrl( null );
                        }
                    }
                }
            }
        }

        while ( getNexusUrl() == null || getNexusUrl().trim().length() < 1 )
        {
            try
            {
                setNexusUrl( getPrompter().prompt( "Nexus URL" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }

            try
            {
                connect();
            }
            catch ( RESTLightClientException e )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "Failed to connect using URL: " + getNexusUrl(), e );
                }
                else
                {
                    getLog().info( "Failed to connect using URL: " + getNexusUrl() );
                }

                setNexusUrl( null );
            }
        }
    }

    protected StageRepository select( final List<StageRepository> stageRepos, final String basicPrompt )
        throws MojoExecutionException
    {
        if ( stageRepos == null || stageRepos.isEmpty() )
        {
            return null;
        }
        
        LinkedHashMap<String, StageRepository> repoMap = new LinkedHashMap<String, StageRepository>();
        StringBuilder menu = new StringBuilder();
        List<String> choices = new ArrayList<String>();

        menu.append( "\n\n\nAvailable Staging Repositories:\n\n" );
        
        int i = 0;
        for ( StageRepository repo : stageRepos )
        {
            ++i;
            repoMap.put( Integer.toString( i ), repo );
            choices.add( Integer.toString( i ) );

            menu.append( "\n" ).append( i ).append( ": " ).append( listRepo( repo ) ).append( "\n" );
        }
        
        menu.append( "\n\n" );

        String choice = null;
        while ( choice == null || !repoMap.containsKey( choice ) )
        {
            getLog().info( menu.toString() );
            try
            {
                choice = getPrompter().prompt( basicPrompt, choices, "1" );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

        return repoMap.get( choice );
    }

}