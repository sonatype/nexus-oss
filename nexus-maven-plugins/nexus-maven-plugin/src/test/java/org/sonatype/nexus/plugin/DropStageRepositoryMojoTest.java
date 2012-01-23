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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.jdom.JDOMException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;
import org.sonatype.nexus.plugin.discovery.fixture.DefaultDiscoveryFixture;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

/**
 * @author plynch
 */
public class DropStageRepositoryMojoTest
    extends NexusMojoTestSupport
{

    private DropStageRepositoryMojo newMojo()
    {
        DropStageRepositoryMojo mojo = new DropStageRepositoryMojo();

        mojo.setPrompter( prompter );
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter ) );
        mojo.setDispatcher( secDispatcher );

        return mojo;
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

        DropStageRepositoryMojo mojo = newMojo();

        prompter.addExpectation( "1", "" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        Settings settings = new Settings();
        startProxyServer( useProxyAuth );
        settings.addProxy( getMavenSettingsProxy( useProxyAuth ) );
        mojo.setSettings( settings );
        mojo.setDescription( "Reason for dropping repo cannot be null" );

        runMojo( mojo );

        List<String> proxyUris = proxyServer.getAccessedUris();
        assertThat( proxyUris, hasSize( 12 ) );
        assertThat(
            proxyUris,
            allOf( hasItem( endsWith( "service/local/staging/profiles" ) ),
                hasItem( endsWith( "service/local/staging/profiles/112cc490b91265a1/drop" ) ),
                hasItem( endsWith( "service/local/staging/profile_repositories/112cc490b91265a1" ) ) ) );

    }

    private void runMojo( final DropStageRepositoryMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        // get the staging profiles
        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "drop/profile-list.xml" ) );

        conversation.add( repoListGet );

        // get the staging profile repo list
        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "drop/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        POSTFixture dropPost = new POSTFixture( getExpectedUser(), getExpectedPassword() );
        dropPost.setExactURI( StageClient.PROFILES_PATH + "/112cc490b91265a1" + StageClient.STAGE_REPO_DROP_ACTION );

        if ( "1.3.1".equals( getTestNexusAPIVersion() ) )
        {
            dropPost.setRequestDocument( readTestDocumentResource( "drop/drop-repo-old.xml" ) );
        }
        else
        {
            dropPost.setRequestDocument( readTestDocumentResource( "drop/drop-repo-new.xml" ) );
        }

        dropPost.setResponseStatus( 201 );

        conversation.add( dropPost );

        repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "drop/profile-list-dropped.xml" ) );
        conversation.add( repoListGet );

        reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "drop/profile-repo-list-dropped.xml" ) );
        conversation.add( reposGet );

        fixture.setConversation( conversation );

        mojo.execute();
    }
}
