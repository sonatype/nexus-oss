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

import static org.junit.Assert.fail;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.jdom.JDOMException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;
import org.sonatype.nexus.plugin.discovery.fixture.DefaultDiscoveryFixture;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;

public class PromoteToStageProfileMojoTest
    extends NexusMojoTestSupport
{

    @Test
    public void simplestUseCase()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "The description" );

        runMojo( mojo );
    }

    @Test
    public void mavenProxySupportWithAuth()
        throws StartingException, JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        mavenProxySupportTest( true );
    }

    @Test
    public void mavenProxySupportWithoutAuth()
        throws StartingException, JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        mavenProxySupportTest( false );
    }

    private void mavenProxySupportTest( boolean useProxyAuth )
        throws StartingException, JDOMException, IOException, MojoExecutionException
    {

        PromoteToStageProfileMojo mojo = newMojo();

        Settings settings = new Settings();
        startProxyServer( useProxyAuth );
        settings.addProxy( getMavenSettingsProxy( useProxyAuth ) );
        mojo.setSettings( settings );

        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "The description" );

        runMojo( mojo );

        List<String> proxyUris = proxyServer.getAccessedUris();
        assertThat( proxyUris, hasSize( 12 ) );
        assertThat(
            proxyUris,
            allOf( hasItem( endsWith( "service/local/staging/profiles" ) ),
                hasItem( endsWith( "service/local/status" ) ),
                hasItem( endsWith( "service/local/staging/profile_repositories/profile1" ) ),
                hasItem( endsWith( "service/local/staging/bulk/promote" ) ) ) );

    }

    @Test
    public void promptForGroupProfile()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        prompter.addExpectation( "Build Promotion Profile", "1" );
        // mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "The description" );

        runMojo( mojo );
    }

    @Test
    public void promptForRepository()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        prompter.setUseOrder( true );
        prompter.addExpectation( "Repository: ", "1" );
        prompter.addExpectation( "Add another Repository?", "y" );
        prompter.addExpectation( "Repository:", "2" );
        prompter.addExpectation( "Add another Repository?", "n" );

        // prompter.addExpectation( "Repository", "0" );
        mojo.setStagingBuildPromotionProfileId( "profile3" );
        // mojo.getRepositoryIds().add( "repoId1" );
        // mojo.getRepositoryIds().add( "repoId2" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "The description" );

        runMojo( mojo );
    }

    @Test
    public void promptForDescription()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        prompter.addExpectation( "Description", "The description" );
        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        // mojo.setDescription( "The description" );

        runMojo( mojo );
    }

    @Test
    public void promptForPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        prompter.addExpectation( "Are you sure you want to use the Nexus URL", "y" );
        prompter.addExpectation( "Enter Username [" + getExpectedUser() + "]", getExpectedUser() );
        prompter.addExpectation( "Enter Password", getExpectedPassword() );

        PromoteToStageProfileMojo mojo = newMojo();

        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );
        mojo.setDescription( "The description" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );

        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        prompter.addExpectation( "Nexus URL", getBaseUrl() );
        prompter.addExpectation( "Enter Username [" + getExpectedUser() + "]", getExpectedUser() );
        prompter.addExpectation( "Enter Password", getExpectedPassword() );

        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );
        mojo.setDescription( "The description" );

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        PromoteToStageProfileMojo mojo = newMojo();

        String serverId = "server";

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( getExpectedPassword() );

        Settings settings = new Settings();
        settings.addServer( server );

        mojo.setSettings( settings );
        mojo.setServerAuthId( serverId );

        mojo.setStagingBuildPromotionProfileId( "profile3" );
        mojo.getRepositoryIds().add( "repoId1" );
        mojo.getRepositoryIds().add( "repoId2" );
        mojo.setDescription( "The description" );

        mojo.setNexusUrl( getBaseUrl() );

        runMojo( mojo );
    }

    private PromoteToStageProfileMojo newMojo()
    {
        PromoteToStageProfileMojo mojo = new PromoteToStageProfileMojo();

        mojo.setPrompter( prompter );
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter ) );
        mojo.setDispatcher( secDispatcher );

        return mojo;
    }

    protected String getTestNexusAPIVersion()
    {
        return "1.7.2";
    }

    private void runMojo( final PromoteToStageProfileMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        conversation.add( getVersionCheckFixture() );

        GETFixture profileListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        profileListGet.setExactURI( StageClient.PROFILES_PATH );
        profileListGet.setResponseDocument( readTestDocumentResource( "promoteToGroup/profile-list.xml" ) );
        conversation.add( profileListGet );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "promoteToGroup/profile-list.xml" ) );
        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "profile1" );
        reposGet.setResponseDocument( readTestDocumentResource( "finish/profile-repo-list.xml" ) );
        conversation.add( reposGet );

        // reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        // reposGet.setExactURI( StageClient.PROFILES_PATH );
        // reposGet.setResponseDocument( readTestDocumentResource( "promoteToGroup/profile-list.xml" ) );
        // conversation.add( reposGet );

        POSTFixture finishPost = new POSTFixture( getExpectedUser(), getExpectedPassword() );
        finishPost.setExactURI( StageClient.STAGE_REPO_BULK_PROMOTE );
        finishPost.setRequestDocument( readTestDocumentResource( "promoteToGroup/promote-to-group-request.xml" ) );
        finishPost.setResponseStatus( 201 );
        conversation.add( finishPost );

        repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "promoteToGroup/profile-list-result.xml" ) );
        conversation.add( repoListGet );

        fixture.setConversation( conversation );

        mojo.execute();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }
    }

}
