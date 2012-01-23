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

import static junit.framework.Assert.fail;

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
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;

public class ListStageRepositoriesMojoTest
    extends NexusMojoTestSupport
{

    private ListStageRepositoriesMojo newMojo()
    {
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();

        mojo.setPrompter( prompter );
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter ) );
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

        ListStageRepositoriesMojo mojo = newMojo();

        Settings settings = new Settings();
        startProxyServer( useProxyAuth );
        settings.addProxy( getMavenSettingsProxy( useProxyAuth ) );
        mojo.setSettings( settings );

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setNexusUrl( getBaseUrl() );

        runMojo( mojo );

        List<String> proxyUris = proxyServer.getAccessedUris();
        assertThat( proxyUris, hasSize( 10 ) );
        assertThat(
            proxyUris,
            allOf( hasItem( endsWith( "service/local/staging/profiles" ) ),
                hasItem( endsWith( "service/local/status" ) ),
                hasItem( endsWith( "service/local/staging/profile_repositories/112cc490b91265a1" ) ) ) );

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
