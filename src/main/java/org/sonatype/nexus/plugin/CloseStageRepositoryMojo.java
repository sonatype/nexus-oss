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
 * Close a Nexus staging repository so it's available for use by Maven.
 * 
 * @goal staging-close
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class CloseStageRepositoryMojo
    extends AbstractStagingMojo
{

    /**
     * The description for the newly closed staging repository. This will show up in the Nexus UI.
     * 
     * @parameter expression="${description}"
     */
    private String description;

    /**
     * The artifact groupId used to select which open staging repository should be closed.
     * 
     * @parameter expression="${groupId}" default-value="${project.groupId}"
     */
    private String groupId;

    /**
     * The artifact artifactId used to select which open staging repository should be closed.
     * 
     * @parameter expression="${artifactId}" default-value="${project.artifactId}"
     */
    private String artifactId;

    /**
     * The artifact version used to select which open staging repository should be closed.
     * 
     * @parameter expression="${version}" default-value="${project.version}"
     */
    private String version;

    /**
     * If true, the mojo will simply select the first result from the list of open staging repositories that match the
     * given groupId, artifactId, and version. Otherwise, the mojo will prompt the user for input.
     * 
     * @parameter expression="${auto}" default-value="false"
     */
    private boolean auto;

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        StageClient client = getClient();

        List<StageRepository> repos;
        try
        {
            repos = client.getOpenStageRepositoriesForUser( groupId, artifactId, version );
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find open staging repository: " + e.getMessage(), e );
        }
        
        if ( repos != null && !repos.isEmpty() )
        {
            StageRepository repo;
            if ( auto )
            {
                repo = repos.get( 0 );
            }
            else
            {
                repo = select( repos, "Select a repository to close" );
            }
            
            StringBuilder builder = new StringBuilder();
            builder.append( "Closing staging repository for: '" )
                   .append( groupId )
                   .append( ":" )
                   .append( artifactId )
                   .append( ":" )
                   .append( version );

            builder.append( "\n\n-  " );
            builder.append( listRepo( repo ) );

            builder.append( "\n\n" );

            getLog().info( builder.toString() );

            promptForMissingDescription();
            
            try
            {
                client.finishRepository( repo, getDescription() );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to close open staging repository: " + e.getMessage(), e );
            }
        }
        else
        {
            getLog().info( "\n\nNo open staging repositories found. Nothing to do!\n\n" );
        }

        listRepos( groupId, artifactId, version, "The following CLOSED staging repositories were found" );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( final String version )
    {
        this.version = version;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }
    
    private String promptForMissingDescription()
        throws MojoExecutionException
    {
        while ( getDescription() == null || getDescription().trim().length() < 1 )
        {
            try
            {
                setDescription( getPrompter().prompt( "Repository Description" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
        
        return getDescription();
    }

    @Override
    protected void fillMissing()
        throws MojoExecutionException
    {
        super.fillMissing();

        while ( getGroupId() == null || "${project.groupId}".equals( getGroupId() ) )
        {
            try
            {
                setGroupId( getPrompter().prompt( "Group ID" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

        while ( getArtifactId() == null || "${project.artifactId}".equals( getArtifactId() ) )
        {
            try
            {
                setArtifactId( getPrompter().prompt( "Artifact ID" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

        while ( getVersion() == null || "${project.version}".equals( getVersion() ) )
        {
            try
            {
                setVersion( getPrompter().prompt( "Version" ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
    }

}
