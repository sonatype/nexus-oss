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
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

import java.util.List;

public abstract class AbstractStagingMojo
    extends AbstractNexusMojo
{

    /**
     * The base URL for a Nexus Professional instance that includes the nexus-staging-plugin. If missing, the mojo will
     * prompt for this value.
     * 
     * @parameter expression="${nexusUrl}"
     */
    private String nexusUrl;

    private StageClient client;

    public AbstractStagingMojo()
    {
        super();
    }

    public String getNexusUrl()
    {
        return nexusUrl;
    }

    public void setNexusUrl( final String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
    }

    protected synchronized StageClient getClient()
        throws MojoExecutionException
    {

        if ( client == null )
        {
            getLog().info( "Logging into Nexus: " + getNexusUrl() );
            getLog().info( "User: " + getUsername() );

            try
            {
                client = new StageClient( getNexusUrl(), getUsername(), getPassword() );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to open staging client: " + e.getMessage(), e );
            }
        }

        return client;
    }

    protected void listClosedRepos( final String groupId, final String artifactId, final String version )
        throws MojoExecutionException
    {
        List<StageRepository> closedStageRepositories;
        StringBuilder builder = new StringBuilder();

        try
        {
            if ( groupId != null )
            {
                closedStageRepositories = getClient().getClosedStageRepositoriesForUser( groupId, artifactId, version );
                builder.append( "The following FINISHED staging repositories were found for: '" )
                       .append( groupId )
                       .append( ":" )
                       .append( artifactId )
                       .append( ":" )
                       .append( version )
                       .append( "':" );
            }
            else
            {
                closedStageRepositories = getClient().getClosedStageRepositoriesForUser();
                builder.append( "The following FINISHED staging repositories were found: " );
            }
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to list closed staging repositories: " + e.getMessage(), e );
        }

        if ( closedStageRepositories.isEmpty() )
        {
            builder.append( "\n\nNone." );
        }
        else
        {
            for ( StageRepository closedRepo : closedStageRepositories )
            {
                builder.append( "\n\n-  " )
                       .append( closedRepo.getRepositoryId() )
                       .append( " (profile: " )
                       .append( closedRepo.getProfileName() )
                       .append( ")" );

                if ( closedRepo.getUrl() != null )
                {
                    builder.append( "\n   URL:" ).append( closedRepo.getUrl() );
                }

                if ( closedRepo.getDescription() != null )
                {
                    builder.append( "\n   Description:" ).append( closedRepo.getDescription() );
                }
            }
        }

        builder.append( "\n\n" );

        getLog().info( builder.toString() );
    }

    @Override
    protected void fillMissing()
        throws MojoExecutionException
    {
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
        }

        super.fillMissing();
    }
}