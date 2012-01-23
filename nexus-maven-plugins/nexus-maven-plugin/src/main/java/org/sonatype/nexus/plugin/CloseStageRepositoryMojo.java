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
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

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
     * @parameter default-value="Staging Closing ${project.build.finalName}" expression="${nexus.description}"
     */
    private String description;

    /**
     * The artifact groupId used to select which open staging repository should be closed.
     * 
     * @parameter expression="${nexus.groupId}" default-value="${project.groupId}"
     */
    private String groupId;

    /**
     * The artifact artifactId used to select which open staging repository should be closed.
     * 
     * @parameter expression="${nexus.artifactId}" default-value="${project.artifactId}"
     */
    private String artifactId;

    /**
     * The artifact version used to select which open staging repository should be closed.
     * 
     * @parameter expression="${nexus.version}" default-value="${project.version}"
     */
    private String version;

    /**
     * If true, the mojo will simply select the first result from the list of open staging repositories that match the
     * given groupId, artifactId, and version. Otherwise, the mojo will prompt the user for input.
     * 
     * @parameter expression="${auto}" default-value="false"
     * @deprecated Use parameter 'automatic' instead.
     */
    @Deprecated
    private boolean auto;

    protected void doExecute()
        throws MojoExecutionException
    {
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
            StageRepository repo = select( repos, "Select a repository to close", true );
            
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
        if ( isAutomatic()
            && ( getDescription() == null || getVersion() == null || getGroupId() == null || getArtifactId() == null ) )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( "In automatic mode, you must specify the following parameters in your POM " )
              .append( "configuration or on the command line:\n" )
              .append( "\n- groupId (CLI expression: 'nexus.groupId', default value: ${project.groupId})" )
              .append( "\n- artifactId (CLI expression: 'nexus.artifactId', default value: ${project.artifactId})" )
              .append( "\n- version (CLI expression: 'nexus.version', default value: ${project.version})" )
              .append( "\n- description (CLI expression: 'nexus.description')" );

            throw new MojoExecutionException( sb.toString() );
        }

        super.fillMissing();

        while ( !isAutomatic() && ( getGroupId() == null || "${project.groupId}".equals( getGroupId() ) ) )
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

        while ( !isAutomatic() && ( getArtifactId() == null || "${project.artifactId}".equals( getArtifactId() ) ) )
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

        while ( !isAutomatic() && ( getVersion() == null || "${project.version}".equals( getVersion() ) ) )
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

    @Override
    public boolean isAutomatic()
    {
        return super.isAutomatic() || auto;
    }

}
