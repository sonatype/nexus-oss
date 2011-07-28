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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
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
    extends AbstractNexusMojoTest
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

        String master = cipher.encryptAndDecorate( clearTextPassword, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );

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
        mojo.setDiscoverer( new DefaultDiscoveryFixture( secDispatcher, prompter, logger ) );
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
