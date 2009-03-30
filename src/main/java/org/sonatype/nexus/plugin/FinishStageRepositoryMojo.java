package org.sonatype.nexus.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.sonatype.nexus.restlight.common.SimpleRESTClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

import java.util.List;


/**
 * Finish a Nexus staging repository so it's available for use by Maven.
 *
 * @goal finish
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class FinishStageRepositoryMojo
    extends AbstractMojo
{
    
    /**
     * @component roleHint="jline"
     */
    private Prompter prompter;
    
    /**
     * @parameter expression="${nexusUrl}"
     */
    private String nexusUrl;
    
    /**
     * @parameter expression="${username}" default-value="${user.name}"
     */
    private String username;
    
    /**
     * @parameter expression="${serverAuthId}"
     */
    private String serverAuthId;
    
    /**
     * @parameter expression="${password}"
     */
    private String password;
    
    /**
     * @parameter default-value="${project.groupId}"
     * @readonly
     */
    private String groupId;
    
    /**
     * @parameter default-value="${project.artifactId}"
     * @readonly
     */
    private String artifactId;
    
    /**
     * @parameter default-value="${project.version}"
     * @readonly
     */
    private String version;
    
    /**
     * @parameter default-value="${settings}"
     * @readonly
     */
    private Settings settings;
    
    /**
     * @parameter expression="${verboseDebug}" default-value="false"
     */
    private boolean verboseDebug;
    
    public void execute()
        throws MojoExecutionException
    {
        fillMissing();
        
        initLog4j();
        
        StageClient client;
        try
        {
            client = new StageClient( nexusUrl, username, password );
        }
        catch ( SimpleRESTClientException e )
        {
            throw new MojoExecutionException( "Failed to open staging client: " + e.getMessage(), e );
        }
        
        StageRepository openRepo;
        try
        {
            openRepo = client.getOpenStageRepositoryForUser( groupId, artifactId, version );
        }
        catch ( SimpleRESTClientException e )
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
                   .append( openRepo.getProfileId() )
                   .append( ")\n   URL: " )
                   .append( openRepo.getUrl() )
                   .append( "\n\n" );
            
            getLog().info( builder.toString() );
            
            try
            {
                client.finishRepository( openRepo );
            }
            catch ( SimpleRESTClientException e )
            {
                throw new MojoExecutionException( "Failed to finish open staging repository: " + e.getMessage(), e );
            }
        }
        else
        {
            getLog().info( "\n\nNo open staging repositories found. Nothing to do!\n\n" );
        }
        
        List<StageRepository> closedStageRepositories;
        try
        {
            closedStageRepositories = client.getClosedStageRepositoriesForUser( groupId, artifactId, version );
        }
        catch ( SimpleRESTClientException e )
        {
            throw new MojoExecutionException( "Failed to list closed staging repositories: " + e.getMessage(), e );
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append( "The following finished staging repositories were found for: '" )
               .append( groupId )
               .append( ":" )
               .append( artifactId )
               .append( ":" )
               .append( version )
               .append( "':" );
        
        for ( StageRepository closedRepo : closedStageRepositories )
        {
            builder.append( "\n\n-  " )
                   .append( closedRepo.getRepositoryId() )
                   .append( " (profile: " )
                   .append( closedRepo.getProfileId() )
                   .append( ")\n   URL:" )
                   .append( closedRepo.getUrl() );
        }
        
        builder.append( "\n\n" );
        
        getLog().info( builder.toString() );
    }

    private void initLog4j()
    {
        if ( getLog().isDebugEnabled() )
        {
            if ( verboseDebug )
            {
                LogManager.getRootLogger().setLevel( Level.DEBUG );
            }
            else
            {
                LogManager.getRootLogger().setLevel( Level.INFO );
            }
        }
        else
        {
            LogManager.getRootLogger().setLevel( Level.WARN );
        }
        
        if ( !LogManager.getRootLogger().getAllAppenders().hasMoreElements() )
        {
            LogManager.getRootLogger().addAppender( new ConsoleAppender( new SimpleLayout() ) );
        }
    }

    private void fillMissing()
        throws MojoExecutionException
    {
        while ( nexusUrl == null || nexusUrl.trim().length() < 1 )
        {
            try
            {
                nexusUrl = prompter.prompt( "Nexus URL: " );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
        
        if ( serverAuthId != null )
        {
            Server server = settings == null ? null : settings.getServer( serverAuthId );
            if ( server != null )
            {
                getLog().info( "Using authentication information for server: '" + serverAuthId + "'." );
                
                username = server.getUsername();
                password = server.getPassword();
            }
            else
            {
                getLog().debug( "Server entry not found for: '" + serverAuthId + "'." );
            }
        }
        
        while ( password == null || password.trim().length() < 1 )
        {
            try
            {
                password = prompter.promptForPassword( "Password: " );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }
        
    }

    protected Prompter getPrompter()
    {
        return prompter;
    }

    protected void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }

    protected String getNexusUrl()
    {
        return nexusUrl;
    }

    protected void setNexusUrl( String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
    }

    protected String getUsername()
    {
        return username;
    }

    protected void setUsername( String username )
    {
        this.username = username;
    }

    protected String getServerAuthId()
    {
        return serverAuthId;
    }

    protected void setServerAuthId( String serverAuthId )
    {
        this.serverAuthId = serverAuthId;
    }

    protected String getPassword()
    {
        return password;
    }

    protected void setPassword( String password )
    {
        this.password = password;
    }

    protected String getGroupId()
    {
        return groupId;
    }

    protected void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    protected String getArtifactId()
    {
        return artifactId;
    }

    protected void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    protected String getVersion()
    {
        return version;
    }

    protected void setVersion( String version )
    {
        this.version = version;
    }

    protected Settings getSettings()
    {
        return settings;
    }

    protected void setSettings( Settings settings )
    {
        this.settings = settings;
    }

    protected boolean isVerboseDebug()
    {
        return verboseDebug;
    }

    protected void setVerboseDebug( boolean verboseDebug )
    {
        this.verboseDebug = verboseDebug;
    }

}
