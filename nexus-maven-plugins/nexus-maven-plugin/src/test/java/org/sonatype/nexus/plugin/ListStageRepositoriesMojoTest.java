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

import static junit.framework.Assert.fail;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.jdom.JDOMException;
import org.junit.Test;
import org.sonatype.nexus.plugin.discovery.fixture.DefaultDiscoveryFixture;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ListStageRepositoriesMojoTest
    extends AbstractNexusMojoTest
{

    private ListStageRepositoriesMojo newMojo()
    {
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();

        mojo.setPrompter( prompter );
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter, logger ) );
        mojo.setDispatcher( secDispatcher );

        return mojo;
    }

    @Test
    public void simplestUseCase()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = newMojo();
        
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setNexusUrl( getBaseUrl() );

        runMojo( mojo );
    }

    @Test
    public void baseUrlWithTrailingSlash()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        ListStageRepositoriesMojo mojo = newMojo();

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setNexusUrl( getBaseUrl() + "/" );

        mojo.setVerboseDebug( true );
        fixture.setDebugEnabled( true );

        runMojo( mojo );
    }

    @Test
    public void badPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        ListStageRepositoriesMojo mojo = newMojo();

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( "wrong" );
        mojo.setNexusUrl( getBaseUrl() );

        try
        {
            runMojo( mojo );
            fail( "should fail to connect due to bad password" );
        }
        catch ( MojoExecutionException e )
        {
            // expected.
        }
    }

    @Test
    public void promptForPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = newMojo();
        
        prompter.addExpectation( "Are you sure you want to use the Nexus URL", "" );
        prompter.addExpectation( "Enter Username [" + getExpectedUser() + "]", getExpectedUser() );
        prompter.addExpectation( "Enter Password", getExpectedPassword() );
        
        mojo.setUsername( getExpectedUser() );
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = newMojo();
        
        prompter.addExpectation( "Nexus URL", getBaseUrl() );
        prompter.addExpectation( "Enter Username [" + getExpectedUser() + "]", getExpectedUser() );
        prompter.addExpectation( "Enter Password", getExpectedPassword() );

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        
        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = newMojo();
        
        String serverId = "server";
        
        Server server = new Server();
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( getExpectedPassword() );
        
        Settings settings = new Settings();
        settings.addServer( server );
        
        mojo.setSettings( settings );
        mojo.setServerAuthId( serverId );
        
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    private void runMojo( final ListStageRepositoriesMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );
        
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list-closed.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
        
        mojo.execute();
    }
    
}
