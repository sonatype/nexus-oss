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
    private List<String> repositoryIds;

    /**
     * @parameter expression="${description}"
     */
    private String description; 

    /**
     * @parameter expression="${stagingBuildPromotionProfileId}"
     */
    private String stagingBuildPromotionProfileId;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

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
        
        if( profiles == null || profiles.isEmpty() )
        {
            getLog().info( "\n\nNo build promotion profiles found. Nothing to do!\n\n" );
            return;
        }
        
        // select build promotion profile
        promptForStagingBuildPromotionProfileId( profiles );
        
        // select repositories
        // prompt if not already set
        if( (this.getRepositoryIds() == null || this.getRepositoryIds().isEmpty()) )
        {
            promptForRepositoryIds( repos );
        }
        
        // enter description
        promptForDescription();

        StringBuilder builder = new StringBuilder();
        builder.append( "Promoting staging repository to: " ).append( getStagingBuildPromotionProfileId() ).append( ":" );

        builder.append( "\n\n" );
        for ( String repoId : getRepositoryIds() )
        {
            builder.append( "-  " ).append( repoId );    
        }
        
        builder.append( "\n\n" );

        getLog().info( builder.toString() );

        try
        {
            client.promoteRepositories( stagingBuildPromotionProfileId, description, new ArrayList( repositoryIds ) );
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
            catch( NumberFormatException e )
            {
                this.getLog().debug( "Invalid entry: "+ e.getMessage() );
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
            buffer.append( ii + 1 ).append( ".  " ).append( repo.getRepositoryId() ).append( " - " ).append( repo.getDescription() ).append( "\n" );
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
            catch( NumberFormatException e )
            {
                this.getLog().debug( "Invalid entry: "+ e.getMessage() );
            }
        }
    }

    public Set<String> getRepositoryIds()
    {
        if ( ( repositoryIds == null || repositoryIds.isEmpty() ) && super.getRepositoryId() != null )
        {
            this.repositoryIds = Collections.singletonList( getRepositoryId() );
        }
        return new LinkedHashSet<String>( repositoryIds );
    }

    public void setRepositoryIds( Set<String> repositoryIds )
    {
        this.repositoryIds = new ArrayList<String>( repositoryIds );
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
