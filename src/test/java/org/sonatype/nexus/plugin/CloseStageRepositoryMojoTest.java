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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CloseStageRepositoryMojoTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture();

    private final Set<File> toDelete = new HashSet<File>();

    private Log log;

    @Before
    public void setupMojoLog()
    {
        log = new SystemStreamLog()
        {
            @Override
            public boolean isDebugEnabled()
            {
                return true;
            }
        };
    }

    @After
    public void cleanupFiles()
    {
        if ( toDelete != null )
        {
            for ( File f : toDelete )
            {
                try
                {
                    FileUtils.forceDelete( f );
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to delete test file/dir: " + f + ". Reason: " + e.getMessage() );
                }
            }
        }
    }

    @Test
    public void simplestUseCase()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        CloseStageRepositoryMojo mojo = new CloseStageRepositoryMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        prompter.addExpectation( "1", "1" );

        mojo.setPrompter( prompter );
        
        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        
        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( "testuser" );
        mojo.setPassword( "unused" );
        mojo.setDescription( "this is a description" );
        
        runMojo( mojo );
    }

    @Test
    public void promptForPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        CloseStageRepositoryMojo mojo = new CloseStageRepositoryMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Password", "unused" );
        prompter.addExpectation( "1", "1" );
        
        mojo.setPrompter( prompter );
        
        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        mojo.setDescription( "this is a description" );
        
        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( "testuser" );
        
        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        CloseStageRepositoryMojo mojo = new CloseStageRepositoryMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Nexus URL", getBaseUrl() );
        prompter.addExpectation( "1", "1" );
        
        mojo.setPrompter( prompter );
        
        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        mojo.setDescription( "this is a description" );
        
        mojo.setUsername( "testuser" );
        mojo.setPassword( "unused" );
        
        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        CloseStageRepositoryMojo mojo = new CloseStageRepositoryMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        prompter.addExpectation( "1", "1" );

        mojo.setPrompter( prompter );
        
        String serverId = "server";
        
        Server server = new Server();
        server.setId( serverId );
        server.setUsername( "testuser" );
        server.setPassword( "unused" );
        
        Settings settings = new Settings();
        settings.addServer( server );
        
        mojo.setSettings( settings );
        mojo.setServerAuthId( serverId );
        
        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        mojo.setDescription( "this is a description" );
        
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    private void runMojo( final CloseStageRepositoryMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );
        
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "finish/profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture();
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "finish/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        POSTFixture finishPost = new POSTFixture();

        finishPost.setExactURI( StageClient.PROFILES_PATH + "/112cc490b91265a1" + StageClient.STAGE_REPO_FINISH_ACTION );

        if ( "1.3.1".equals( getTestNexusAPIVersion() ) )
        {
            finishPost.setRequestDocument( readTestDocumentResource( "finish/finish-repo-old.xml" ) );
        }
        else
        {
            finishPost.setRequestDocument( readTestDocumentResource( "finish/finish-repo-new.xml" ) );
        }

        finishPost.setResponseStatus( 201 );
        
        conversation.add( finishPost );

        repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "finish/profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        reposGet = new GETFixture();
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "finish/profile-repo-list-closed.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
        
        mojo.execute();
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    protected void printTestName()
    {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        System.out.println( "\n\nRunning: '"
            + ( getClass().getName().substring( getClass().getPackage().getName().length() + 1 ) ) + "#"
            + e.getMethodName() + "'\n\n" );
    }
    
}
