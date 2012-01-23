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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import org.jdom.JDOMException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.plugin.discovery.fixture.DefaultDiscoveryFixture;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;
import org.sonatype.plexus.components.sec.dispatcher.model.io.xpp3.SecurityConfigurationXpp3Writer;

public class CloseStageRepositoryMojoTest
    extends NexusMojoTestSupport
{

    protected static File secFile;

    protected static String encryptedPassword;

    protected static String clearTextPassword = "password";

    protected static String oldSecLocation;

    @BeforeClass
    public static void beforeAll()
        throws PlexusCipherException, IOException
    {
        DefaultPlexusCipher cipher = new DefaultPlexusCipher();

        String master =
            cipher.encryptAndDecorate( clearTextPassword, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );

        SettingsSecurity sec = new SettingsSecurity();
        sec.setMaster( master );

        secFile = getTempFile( "settings-security.", ".xml" );
        secFile.getParentFile().mkdirs();
        secFile.createNewFile();

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( secFile );
            new SecurityConfigurationXpp3Writer().write( writer, sec );
        }
        finally
        {
            IOUtil.close( writer );
        }

        encryptedPassword = cipher.encryptAndDecorate( "password", "password" );

        Properties sysProps = System.getProperties();
        oldSecLocation = sysProps.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );
        sysProps.setProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, secFile.getAbsolutePath() );

        System.setProperties( sysProps );
    }

    @AfterClass
    public static void afterAll()
    {
        if ( oldSecLocation != null )
        {
            Properties sysProps = System.getProperties();
            sysProps.setProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, oldSecLocation );

            System.setProperties( sysProps );
        }

        try
        {
            FileUtils.forceDelete( secFile );
        }
        catch ( IOException e )
        {
        }
    }

    @Test
    public void simplestUseCase()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        CloseStageRepositoryMojo mojo = newMojo();

        prompter.addExpectation( "1", "" );

        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "this is a description" );

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
        prompter.addExpectation( "1", "" );

        CloseStageRepositoryMojo mojo = newMojo();

        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        mojo.setDescription( "this is a description" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );

        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        CloseStageRepositoryMojo mojo = newMojo();

        prompter.addExpectation( "Nexus URL", getBaseUrl() );
        prompter.addExpectation( "Enter Username [" + getExpectedUser() + "]", getExpectedUser() );
        prompter.addExpectation( "Enter Password", getExpectedPassword() );
        prompter.addExpectation( "1", "" );

        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );
        mojo.setDescription( "this is a description" );

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        prompter.addExpectation( "1", "" );

        CloseStageRepositoryMojo mojo = newMojo();

        String serverId = "server";

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( getExpectedPassword() );

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

    @Test
    public void mavenProxySupportWithAuth()
        throws StartingException, JDOMException, IOException, MojoExecutionException, RESTLightClientException,
        InitializationException
    {
        printTestName();
        mavenProxySupportTest( true );
    }

    @Test
    public void mavenProxySupportWithoutAuth()
        throws StartingException, JDOMException, IOException, MojoExecutionException, RESTLightClientException,
        InitializationException
    {
        printTestName();
        mavenProxySupportTest( false );
    }

    public void mavenProxySupportTest( boolean useProxyAuth )
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException, StartingException,
        InitializationException
    {

        printTestName();

        CloseStageRepositoryMojo mojo = newMojo();

        prompter.addExpectation( "1", "" );

        Settings settings = new Settings();
        startProxyServer( useProxyAuth );
        settings.addProxy( getMavenSettingsProxy( useProxyAuth ) );
        mojo.setSettings( settings );

        mojo.setArtifactId( "artifactId" );
        mojo.setGroupId( "group.id" );
        mojo.setVersion( "1" );

        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setDescription( "this is a description" );

        // just make sure the URL my mojo is trying to access is the same server is started
        assertThat( mojo.getNexusUrl(), containsString( String.valueOf( fixture.getPort() ) ) );

        runMojo( mojo );

        List<String> proxyUris = proxyServer.getAccessedUris();
        assertThat( proxyUris, hasSize( 12 ) );
        assertThat(
            proxyUris,
            allOf(
                hasItem( endsWith( "service/local/staging/profile_evaluate?v=1&g=group.id&t=maven2&a=artifactId" ) ),
                hasItem( endsWith( "/service/local/status" ) ) ) );

    }

    @Test
    public void authUsingSettingsEnctypedPasswordWithServerAuthId()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        prompter.addExpectation( "1", "" );

        CloseStageRepositoryMojo mojo = newMojo();

        String serverId = "server";

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( encryptedPassword );

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

    private CloseStageRepositoryMojo newMojo()
    {
        CloseStageRepositoryMojo mojo = new CloseStageRepositoryMojo();

        mojo.setPrompter( prompter );
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter ) );
        mojo.setDispatcher( secDispatcher );

        return mojo;
    }

    private void runMojo( final CloseStageRepositoryMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "finish/profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "finish/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        POSTFixture finishPost = new POSTFixture( getExpectedUser(), getExpectedPassword() );

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

        repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_EVALUATE_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "finish/profile-list-closed.xml" ) );
        conversation.add( repoListGet );

        reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "finish/profile-repo-list-closed.xml" ) );
        conversation.add( reposGet );

        fixture.setConversation( conversation );

        mojo.execute();
    }

}
