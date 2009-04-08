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

/**
 * Promote a finished Nexus staging repository into a permanent Nexus repository for general consumption.
 * 
 * @goal staging-promote
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class PromoteStageRepositoryMojo
    extends AbstractStagingMojo
{

    /**
     * @parameter expression="${targetRepositoryId}"
     */
    private String targetRepositoryId;

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        StageClient client = getClient();

        List<StageRepository> repos;
        try
        {
            repos = client.getClosedStageRepositoriesForUser();
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find closed staging repository: " + e.getMessage(), e );
        }
        
        if ( repos != null && !repos.isEmpty() )
        {
            StageRepository repo = select( repos, "Select a repository to promote" );
            
            promptForPromoteInfo();
            
            StringBuilder builder = new StringBuilder();
            builder.append( "Promoting staging repository to: " ).append( getTargetRepositoryId() ).append( ":" );

            builder.append( "\n\n-  " );
            builder.append( listRepo( repo ) );

            builder.append( "\n\n" );

            getLog().info( builder.toString() );
            
            try
            {
                client.promoteRepository( repo, getTargetRepositoryId() );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to promote staging repository: " + e.getMessage(), e );
            }
        }
        else
        {
            getLog().info( "\n\nNo open staging repositories found. Nothing to do!\n\n" );
        }

        listRepos( null, null, null, "The following CLOSED staging repositories were found" );
    }

    private void promptForPromoteInfo()
        throws MojoExecutionException
    {
        while ( getTargetRepositoryId() == null || getTargetRepositoryId().trim().length() < 1 )
        {
            try
            {
                setTargetRepositoryId( getPrompter().prompt( "Target Repository ID" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
        
    }

    public String getTargetRepositoryId()
    {
        return targetRepositoryId;
    }

    public void setTargetRepositoryId( final String targetRepositoryId )
    {
        this.targetRepositoryId = targetRepositoryId;
    }

}
