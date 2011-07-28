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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

/**
 * Release a finished Nexus staging repository into a permanent Nexus repository for general consumption.
 * 
 * @goal staging-release
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class ReleaseStageRepositoryMojo
    extends AbstractStagingMojo
{

    /**
     * If set to <code>true</code>, allow auto-selection of the repository to promote in cases where no repositoryId has
     * been specified during execution, and only one staged repository is available. <br/>
     * <b>NOTE:</b> Use with care! This can be dangerous!
     * 
     * @parameter expression="${nexus.promote.autoSelectOverride}" default-value="false"
     */
    private boolean promoteAutoSelectOverride;

    /**
     * @parameter expression="${targetRepositoryId}"
     */
    private String targetRepositoryId;


    /**
     * @parameter default-value="Staging Releasing ${project.build.finalName}" expression="${description}"
     */
    private String description;

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
            StageRepository repo = select( repos, "Select a repository to promote", isPromoteAutoSelectOverride() );
            
            promptForPromoteInfo();
            
            StringBuilder builder = new StringBuilder();
            builder.append( "Promoting staging repository to: " ).append( getTargetRepositoryId() ).append( ":" );

            builder.append( "\n\n-  " );
            builder.append( listRepo( repo ) );

            builder.append( "\n\n" );

            getLog().info( builder.toString() );
            
            try
            {
                client.promoteRepository( repo, getTargetRepositoryId(), description );
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

    public boolean isPromoteAutoSelectOverride()
    {
        return promoteAutoSelectOverride;
    }

    public void setPromoteAutoSelectOverride( final boolean promoteAutoSelectOverride )
    {
        this.promoteAutoSelectOverride = promoteAutoSelectOverride;
    }

}
