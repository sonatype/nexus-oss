/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

/**
 * Drop a closed Nexus staging repository.
 *
 * @goal staging-drop
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class DropStageRepositoryMojo
    extends AbstractStagingMojo
{

    /**
     * If set to <code>true</code>, allow auto-selection of the repository to drop in cases where no repositoryId has
     * been specified during execution, and only one staged repository is available. <br/>
     * <b>NOTE:</b> Use with care! This can be dangerous!
     *
     * @parameter expression="${nexus.drop.autoSelectOverride}" default-value="false"
     */
    private boolean dropAutoSelectOverride;

    /**
     * @parameter default-value="Staging Dropping ${project.build.finalName}" expression="${description}"
     */
    private String description;

    protected void doExecute()
        throws MojoExecutionException
    {
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
            StageRepository repo = select( repos, "Select a repository to drop", isDropAutoSelectOverride() );

            StringBuilder builder = new StringBuilder();
            builder.append( "Dropping staged repository: " );

            builder.append( "\n\n-  " );
            builder.append( listRepo( repo ) );

            builder.append( "\n\n" );

            getLog().info( builder.toString() );

            try
            {
                client.dropRepository( repo, description );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to drop staging repository: " + e.getMessage(), e );
            }
        }
        else
        {
            getLog().info( "\n\nNo closed staging repositories found. Nothing to do!\n\n" );
        }

        listRepos( null, null, null, "The following CLOSED staging repositories were found" );
    }

    public boolean isDropAutoSelectOverride()
    {
        return dropAutoSelectOverride;
    }

    public void setDropAutoSelectOverride( final boolean dropAutoSelectOverride )
    {
        this.dropAutoSelectOverride = dropAutoSelectOverride;
    }

    public void setDescription( final String desc )
    {
        this.description = desc;
    }

}
