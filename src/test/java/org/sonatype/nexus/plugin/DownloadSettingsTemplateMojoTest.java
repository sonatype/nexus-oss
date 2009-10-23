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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.restlight.m2settings.M2SettingsClient;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.icu.text.SimpleDateFormat;

public class DownloadSettingsTemplateMojoTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

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
    public void backupExistingSettingsInUserConfigDir()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        mojo.setDestination( SettingsDestination.user.toString() );

        mojo.setBackupFormat( "yyyyMMdd" );
        mojo.setDoBackup( true );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File confDir = File.createTempFile( "download-settings-template.maven-conf", ".test.dir" );
        confDir.delete();
        confDir.mkdirs();

        toDelete.add( confDir );

        mojo.setMavenUserConf( confDir );

        File settingsFile = new File( confDir, "settings.xml" );

        Document emptySettings = new Document().setRootElement( new Element( "settings" ) );

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( settingsFile );
            new XMLOutputter( Format.getPrettyFormat() ).output( emptySettings, writer );
        }
        finally
        {
            IOUtil.close( writer );
        }

        runMojoTest( mojo, token, settingsFile );

        String suffix = new SimpleDateFormat( mojo.getBackupFormat() ).format( new Date() );
        File backupSettingsFile = new File( confDir, "settings.xml." + suffix );

        assertTrue( "Backup file: " + backupSettingsFile + " should have been created.", backupSettingsFile.exists() );

        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        assertEquals( outputter.outputString( emptySettings ),
                      outputter.outputString( readTestDocumentFile( backupSettingsFile ) ) );
    }

    @Test
    public void getSettingsTemplatePromptForMissingURL()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();

        ExpectPrompter prompter = new ExpectPrompter();
        prompter.addExpectation( "URL", getTemplateURL( token ) );

        mojo.setPrompter( prompter );

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File target = File.createTempFile( "download-settings-template.", ".test.xml" );
        mojo.setTarget( target );

        toDelete.add( target );

        runMojoTest( mojo, token, target );

        prompter.verifyPromptsUsed();
    }

    @Test
    public void getSettingsTemplatePromptForMissingPassword()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();

        ExpectPrompter prompter = new ExpectPrompter();
        prompter.addExpectation( "Password", getExpectedPassword() );

        mojo.setPrompter( prompter );

        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File target = File.createTempFile( "download-settings-template.", ".test.xml" );
        mojo.setTarget( target );

        toDelete.add( target );

        runMojoTest( mojo, token, target );

        prompter.verifyPromptsUsed();
    }

    @Test
    public void getSettingsTemplateUsingServerFromExistingSettings()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        
        String serverId = "server";
        mojo.setServerAuthId( serverId );

        Settings settings = new Settings();
        
        Server server = new Server();
        
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( getExpectedPassword() );
        
        settings.addServer( server );
        
        mojo.setSettings( settings );

        File target = File.createTempFile( "download-settings-template.", ".test.xml" );
        mojo.setTarget( target );

        toDelete.add( target );

        runMojoTest( mojo, token, target );
    }

    @Test
    public void getSettingsTemplateToCustomTarget()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File target = File.createTempFile( "download-settings-template.", ".test.xml" );
        mojo.setTarget( target );

        toDelete.add( target );

        runMojoTest( mojo, token, target );
    }

    @Test
    public void getSettingsTemplateToGlobalConfigDirWithGlobalDestination()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        mojo.setDestination( SettingsDestination.global.toString() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File confDir = File.createTempFile( "download-settings-template.maven-conf", ".test.dir" );
        confDir.delete();
        confDir.mkdirs();

        toDelete.add( confDir );

        mojo.setMavenHomeConf( confDir );

        File settingsFile = new File( confDir, "settings.xml" );

        runMojoTest( mojo, token, settingsFile );
    }

    @Test
    public void getSettingsTemplateToUserConfigDirWithUserDestination()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        mojo.setDestination( SettingsDestination.user.toString() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File confDir = File.createTempFile( "download-settings-template.maven-conf", ".test.dir" );
        confDir.delete();
        confDir.mkdirs();

        toDelete.add( confDir );

        mojo.setMavenUserConf( confDir );

        File settingsFile = new File( confDir, "settings.xml" );

        runMojoTest( mojo, token, settingsFile );
    }

    @Test
    public void getSettingsTemplateToUserConfigDirWithUnspecifiedDestination()
        throws JDOMException, IOException, MojoExecutionException
    {
        printTestName();
        String token = "testToken";

        DownloadSettingsTemplateMojo mojo = new DownloadSettingsTemplateMojo();
        mojo.setUrl( getTemplateURL( token ) );
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );

        Settings settings = new Settings();
        mojo.setSettings( settings );

        File confDir = File.createTempFile( "download-settings-template.maven-conf", ".test.dir" );
        confDir.delete();
        confDir.mkdirs();

        toDelete.add( confDir );

        mojo.setMavenUserConf( confDir );

        File settingsFile = new File( confDir, "settings.xml" );

        runMojoTest( mojo, token, settingsFile );
    }

    private void runMojoTest( final DownloadSettingsTemplateMojo mojo, final String token, final File checkFile )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture settingsGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        settingsGet.setExactURI( M2SettingsClient.SETTINGS_TEMPLATE_BASE + token + M2SettingsClient.GET_CONTENT_ACTION );

        Document testDoc = readTestDocumentResource( "settings/settings-template-" + token + ".xml" );

        settingsGet.setResponseDocument( testDoc );

        conversation.add( settingsGet );

        fixture.setConversation( conversation );

        mojo.execute();

        Document doc = readTestDocumentFile( checkFile );

        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        assertEquals( outputter.outputString( testDoc ), outputter.outputString( doc ) );
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

    protected String getTemplateURL( final String token )
    {
        return getBaseUrl() + M2SettingsClient.SETTINGS_TEMPLATE_BASE + token + M2SettingsClient.GET_CONTENT_ACTION;
    }

}
