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

/**
 * Finish a Nexus staging repository so it's available for use by Maven.
 * 
 * @goal staging-finish
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class FinishStageRepositoryMojo
    extends AbstractStagingMojo
{

    /**
     * The description for the newly finished staging repository. This will show up in the Nexus UI.
     * 
     * @parameter expression="${description}"
     */
    private String description;

    /**
     * The artifact groupId used to select which open staging repository should be finished.
     * 
     * @parameter default-value="${project.groupId}"
     * @readonly
     */
    private String groupId;

    /**
     * The artifact artifactId used to select which open staging repository should be finished.
     * 
     * @parameter default-value="${project.artifactId}"
     * @readonly
     */
    private String artifactId;

    /**
     * The artifact version used to select which open staging repository should be finished.
     * 
     * @parameter default-value="${project.version}"
     * @readonly
     */
    private String version;

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        StageClient client = getClient();

        StageRepository openRepo;
        try
        {
            openRepo = client.getOpenStageRepositoryForUser( groupId, artifactId, version );
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find open staging repository: " + e.getMessage(), e );
        }

        if ( openRepo != null )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( "Finishing staging repository for: '" )
                   .append( groupId )
                   .append( ":" )
                   .append( artifactId )
                   .append( ":" )
                   .append( version )
                   .append( "':\n\n-  " )
                   .append( openRepo.getRepositoryId() )
                   .append( " (profile: " )
                   .append( openRepo.getProfileName() )
                   .append( ")" );

            if ( openRepo.getUrl() != null )
            {
                builder.append( "\n   URL:" ).append( openRepo.getUrl() );
            }

            if ( openRepo.getDescription() != null )
            {
                builder.append( "\n   Description:" ).append( openRepo.getDescription() );
            }

            builder.append( "\n\n" );

            getLog().info( builder.toString() );

            try
            {
                client.finishRepository( openRepo, description );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to finish open staging repository: " + e.getMessage(), e );
            }
        }
        else
        {
            getLog().info( "\n\nNo open staging repositories found. Nothing to do!\n\n" );
        }

        listClosedRepos( groupId, artifactId, version );
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

    @Override
    protected void fillMissing()
        throws MojoExecutionException
    {
        super.fillMissing();

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
    }

}
