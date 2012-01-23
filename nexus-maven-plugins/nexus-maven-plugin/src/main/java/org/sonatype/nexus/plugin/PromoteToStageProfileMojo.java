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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugin.util.PromptUtil;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageProfile;
import org.sonatype.nexus.restlight.stage.StageRepository;

/**
 * Promote a finished Nexus staging repository into a permanent Nexus repository for general consumption.
 *
 * @goal staging-build-promotion
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class PromoteToStageProfileMojo
    extends AbstractStagingMojo
{

    /**
     * @parameter
     */
    private Set<String> repositoryIds = new LinkedHashSet<String>();

    /**
     * @parameter default-value="Staging Promoting ${project.build.finalName}" expression="${description}"
     */
    private String description;

    /**
     * @parameter expression="${stagingBuildPromotionProfileId}"
     */
    private String stagingBuildPromotionProfileId;

    protected void doExecute()
        throws MojoExecutionException
    {
        StageClient client = getClient();

        List<StageProfile> profiles;
        try
        {
            profiles = this.getClient().getBuildPromotionProfiles();
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find any build promotion profiles.: " + e.getMessage(), e );
        }

        List<StageRepository> repos;
        try
        {
            repos = client.getClosedStageRepositories();
            Collections.sort( repos, new BeanComparator( "repositoryId" ) );
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find closed staging repository: " + e.getMessage(), e );
        }

        // guard
        if ( repos == null || repos.isEmpty() )
        {
            getLog().info( "\n\nNo closed repositories found. Nothing to do!\n\n" );
            return;
        }

        if ( profiles == null || profiles.isEmpty() )
        {
            getLog().info( "\n\nNo build promotion profiles found. Nothing to do!\n\n" );
            return;
        }

        // select build promotion profile
        promptForStagingBuildPromotionProfileId( profiles );

        // select repositories
        // prompt if not already set
        if ( ( this.getRepositoryIds() == null || this.getRepositoryIds().isEmpty() ) )
        {
            promptForRepositoryIds( repos );
        }

        // enter description
        promptForDescription();

        StringBuilder builder = new StringBuilder();
        builder.append( "Promoting staging repository to: " ).append( getStagingBuildPromotionProfileId() ).append(
            ":" );

        builder.append( "\n\n" );
        for ( String repoId : getRepositoryIds() )
        {
            builder.append( "-  " ).append( repoId );
        }

        builder.append( "\n\n" );

        getLog().info( builder.toString() );

        try
        {
            client.promoteRepositories( stagingBuildPromotionProfileId, description, new ArrayList<String>(
                repositoryIds ) );
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to promote staging repositories: " + e.getMessage(), e );
        }

        listRepos( null, null, null, "The following CLOSED staging repositories were found" );

    }

    private void promptForStagingBuildPromotionProfileId( List<StageProfile> profiles )
        throws MojoExecutionException
    {

        StringBuffer buffer = new StringBuffer();
        buffer.append( "Available Build Promotion Profiles:\n" );
        for ( int ii = 0; ii < profiles.size(); ii++ )
        {
            StageProfile profile = profiles.get( ii );
            buffer.append( ii + 1 ).append( ".  " ).append( profile.getName() ).append( "\n" );
        }

        while ( getStagingBuildPromotionProfileId() == null
            || StringUtils.isEmpty( getStagingBuildPromotionProfileId().trim() ) )
        {

            try
            {
                // show possible answers
                getPrompter().showMessage( buffer.toString() );

                String answer = getPrompter().prompt( "Build Promotion Profile: " );
                int pos = Integer.parseInt( answer ) - 1;

                // validate the result
                if ( pos >= 0 && pos < profiles.size() )
                {
                    setStagingBuildPromotionProfileId( profiles.get( pos ).getProfileId() );
                }
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
            catch ( NumberFormatException e )
            {
                this.getLog().debug( "Invalid entry: " + e.getMessage() );
            }
        }
    }

    private void promptForDescription()
        throws MojoExecutionException
    {
        while ( getDescription() == null || getDescription().trim().length() < 1 )
        {
            try
            {
                setDescription( getPrompter().prompt( "Description: " ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
    }

    private void promptForRepositoryIds( List<StageRepository> allClosedRepos )
        throws MojoExecutionException
    {
        boolean finished = false;
        // init the list
        setRepositoryIds( new LinkedHashSet<String>() );

        StringBuffer buffer = new StringBuffer();
        buffer.append( "Closed Staging Repositories:\n" );
        for ( int ii = 0; ii < allClosedRepos.size(); ii++ )
        {
            StageRepository repo = allClosedRepos.get( ii );
            buffer.append( ii + 1 ).append( ".  " ).append( repo.getRepositoryId() ).append( " - " ).append(
                repo.getDescription() ).append( "\n" );
        }

        while ( !finished )
        {
            try
            {
                getPrompter().showMessage( buffer.toString() );

                String answer = getPrompter().prompt( "Repository: " );
                int pos = Integer.parseInt( answer ) - 1;

                // validate the result
                if ( pos >= 0 && pos < allClosedRepos.size() )
                {
                    getRepositoryIds().add( allClosedRepos.get( pos ).getRepositoryId() );

                    if ( !PromptUtil.booleanPrompt( getPrompter(), "Add another Repository? [Y/n]", Boolean.TRUE ) )
                    {
                        // signal end of loop
                        finished = true;
                    }
                }

            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
            catch ( NumberFormatException e )
            {
                this.getLog().debug( "Invalid entry: " + e.getMessage() );
            }
        }
    }

    public Set<String> getRepositoryIds()
    {
        if ( ( repositoryIds == null || repositoryIds.isEmpty() ) && super.getRepositoryId() != null )
        {
            this.repositoryIds = Collections.singleton( getRepositoryId() );
        }

        return repositoryIds;
    }

    public void setRepositoryIds( Set<String> repositoryIds )
    {
        this.repositoryIds = repositoryIds;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getStagingBuildPromotionProfileId()
    {
        return stagingBuildPromotionProfileId;
    }

    public void setStagingBuildPromotionProfileId( String stagingBuildPromotionProfileId )
    {
        this.stagingBuildPromotionProfileId = stagingBuildPromotionProfileId;
    }

}
