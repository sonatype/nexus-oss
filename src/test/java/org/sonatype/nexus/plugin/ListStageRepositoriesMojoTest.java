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
import org.sonatype.nexus.plugin.ListStageRepositoriesMojo;
import org.sonatype.nexus.restlight.common.SimpleRESTClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ListStageRepositoriesMojoTest
    extends AbstractRESTTest
{

    private ConversationalFixture fixture = new ConversationalFixture();

    private Set<File> toDelete = new HashSet<File>();

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
        throws JDOMException, IOException, SimpleRESTClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( "testuser" );
        mojo.setPassword( "unused" );
        
        runMojo( mojo );
    }

    @Test
    public void promptForPassword()
        throws JDOMException, IOException, SimpleRESTClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Password", "unused" );
        
        mojo.setPrompter( prompter );
        
        mojo.setNexusUrl( getBaseUrl() );
        mojo.setUsername( "testuser" );
        
        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, SimpleRESTClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Nexus URL", getBaseUrl() );
        
        mojo.setPrompter( prompter );
        
        mojo.setUsername( "testuser" );
        mojo.setPassword( "unused" );
        
        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, SimpleRESTClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        String serverId = "server";
        
        Server server = new Server();
        server.setId( serverId );
        server.setUsername( "testuser" );
        server.setPassword( "unused" );
        
        Settings settings = new Settings();
        settings.addServer( server );
        
        mojo.setSettings( settings );
        mojo.setServerAuthId( serverId );
        
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    private void runMojo( ListStageRepositoriesMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );
        
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture();
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        repoListGet = new GETFixture();
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        reposGet = new GETFixture();
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list-closed.xml" ) );

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
