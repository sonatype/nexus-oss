package org.sonatype.nexus.integrationtests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.NexusStateUtil;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data START NOTE,
 * this class is not really abstract so I can work around a the <code>@BeforeClass</code>, <code>@AfterClass</code> issues, this should be refactored a little, but it might be ok, if we switch to TestNg
 */
public class AbstractNexusIntegrationTest
{

    private PlexusContainer container;

    private Map<String, Object> context;

    private String basedir;

    private static boolean NEEDS_INIT = false;

    private static boolean NEEDS_HARD_STOP = false;

    public static final String REPOSITORY_RELATIVE_URL = "content/repositories/";

    public static final String GROUP_REPOSITORY_RELATIVE_URL = "content/groups/";

    public String testRepositoryId;

    protected static String nexusBaseDir;

    protected static String baseNexusUrl;

    private String nexusTestRepoUrl;

    protected static String nexusWorkDir;

    static
    {
        nexusBaseDir = TestProperties.getString( "nexus.base.dir" );
        baseNexusUrl = TestProperties.getString( "nexus.base.url" );
        nexusWorkDir = TestProperties.getString( "nexus.work.dir" );
    }

    public static final String RELATIVE_CONF_DIR = "runtime/apps/nexus/conf";

    public static final String RELATIVE_WORK_CONF_DIR = "runtime/work/conf";

    protected AbstractNexusIntegrationTest()
    {
        this( "nexus-test-harness-repo" );
    }

    protected AbstractNexusIntegrationTest( String testRepositoryId )
    {
        this.setupContainer();

        // we also need to setup a couple fields, that need to be pulled out of a bundle
        this.testRepositoryId = testRepositoryId;
        this.nexusTestRepoUrl = baseNexusUrl + REPOSITORY_RELATIVE_URL + testRepositoryId + "/";

    }

    /**
     * To me this seems like a bad hack around this problem. I don't have any other thoughts though. <BR/>If you see
     * this and think: "Wow, why did he to that instead of XYZ, please let me know." <BR/> The issue is that we want to
     * init the tests once (to start/stop the app) and the <code>@BeforeClass</code> is static, so we don't have access to the package name of the running tests. We are going to
     *              use the package name to find resources for additional setup. NOTE: With this setup running multiple
     *              Test at the same time is not possible.
     * @throws Exception
     */
    @Before
    public void oncePerClassSetUp()
        throws Exception
    {
        synchronized ( AbstractNexusIntegrationTest.class )
        {
            if ( NEEDS_INIT )
            {
                HashMap<String, String> variables = new HashMap<String, String>();
                variables.put( "test-harness-id", this.getTestId() );

                // clean common work dir
                // this.cleanWorkDir();

                this.copyConfigFile( "nexus.xml", RELATIVE_WORK_CONF_DIR );

                // copy security config
                this.copyConfigFile( "security.xml", RELATIVE_WORK_CONF_DIR );

                this.copyConfigFile( "log4j.properties", variables );

                if ( TestContainer.getInstance().getTestContext().isSecureTest() )
                {
                    NexusConfigUtil.enableSecurity( true );
                }

                // start nexus
                this.startNexus();

                // deploy artifacts
                this.deployArtifacts();

                // TODO: we can remove this now that we have the soft restart
                NEEDS_INIT = false;
            }
        }
    }

    private boolean isSecurityTest()
    {
        return TestContainer.getInstance().getTestContext().isSecureTest();
    }

    protected static void cleanWorkDir()
        throws IOException
    {
        File workDir = new File( AbstractNexusIntegrationTest.nexusWorkDir );

        // to make sure I don't delete all my MP3's and pictures, or totally screw anyone.
        // check for 'target' and not allow any '..'
        if ( workDir.getAbsolutePath().lastIndexOf( "target" ) != -1
            && workDir.getAbsolutePath().lastIndexOf( ".." ) == -1 )
        {
            // delete work dir
            FileUtils.deleteDirectory( workDir );
        }
    }

    private void deployArtifacts()
        throws IOException, XmlPullParserException, ConnectionException, AuthenticationException,
        TransferFailedException, ResourceDoesNotExistException, AuthorizationException, ComponentLookupException
    {
        // test the test directory
        File projectsDir = this.getTestResourceAsFile( "projects" );
        System.out.println( "projectsDir: " + projectsDir );

        // if null there is nothing to deploy...
        if ( projectsDir != null )
        {

            // we have the parent dir, for each child (one level) we need to grab the pom.xml out of it and parse it,
            // and then deploy the artifact, sounds like fun, right!

            File[] projectFolders = projectsDir.listFiles( new FileFilter()
            {

                public boolean accept( File pathname )
                {
                    return ( !pathname.getName().endsWith( ".svn" ) && pathname.isDirectory() && new File( pathname,
                                                                                                           "pom.xml" ).exists() );
                }
            } );

            for ( int ii = 0; ii < projectFolders.length; ii++ )
            {
                File project = projectFolders[ii];

                // we already check if the pom.xml was in here.
                File pom = new File( project, "pom.xml" );

                MavenXpp3Reader reader = new MavenXpp3Reader();
                FileInputStream fis = new FileInputStream( pom );
                Model model = reader.read( new FileInputStream( pom ) );
                fis.close();

                // a helpful note so you don't need to dig into the code to much.
                if ( model.getDistributionManagement() == null
                    || model.getDistributionManagement().getRepository() == null )
                {
                    Assert.fail( "The test artifact is either missing or has an invalid Distribution Management section." );
                }
                String repositoryId = model.getDistributionManagement().getRepository().getId();
                String deployUrl = model.getDistributionManagement().getRepository().getUrl();

                // FIXME, this needs to be fluffed up a little, should add the classifier, etc.
                String artifactFileName = model.getArtifactId() + "." + model.getPackaging();
                File artifactFile = new File( project, artifactFileName );

                System.out.println( "wow, this is working: " + artifactFile );

                Gav gav =
                    new Gav( model.getGroupId(), model.getArtifactId(), model.getVersion(), null, model.getPackaging(),
                             0, new Date().getTime(), model.getName(), false, false, null, false, null );

                // the Restlet Client does not support multipart forms:
                // http://restlet.tigris.org/issues/show_bug.cgi?id=71

                int status = DeployUtils.deployUsingPomWithRest( deployUrl, repositoryId, gav, artifactFile, pom );

                // Check to makes sure we get a 201
                Assert.assertTrue( "Test-Harness failed to deploy artifact: " + model.getArtifactId()
                    + ", with a status code of: " + status + ", expected: 201", status == HttpStatus.SC_CREATED );

            }

        }
    }

    @After
    public void afterTest()
        throws Exception
    {
        // reset this for each test
        TestContainer.getInstance().getTestContext().useAdminForRequests();
    }

    private void startNexus()
        throws Exception
    {

        // if nexus is running but stopped we only want to do a softstart
        // and we don't want to start if it is already running.

        try
        {
            if ( NexusStateUtil.isNexusRunning() )
            {
                // we have nothing to do if its running
                return;
            }
            else
            {
                NexusStateUtil.doSoftStart();
            }
        }
        catch ( IOException e )
        {
            // nexus is not running....
            // that is ok, most likely someone ran a single test from eclipse

            // we need a hard start
            NEEDS_HARD_STOP = true;

            System.out.println( "***************************" );
            System.out.println( "*\n*" );
            System.out.println( "*  DOING A HARD START OF NEXUS." );
            System.out.println( "*  If your not running a single test manually, then something bad happened" );
            System.out.println( "*\n*" );
            System.out.println( "***************************" );

            ForkedAppBooter appBooter =
                (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
            appBooter.start();
        }
    }

    private void stopNexus()
        throws Exception
    {

        // craptastic state machine
        if ( !NEEDS_HARD_STOP )
        {
            // normal flow is to soft stop
            NexusStateUtil.doSoftStop();
        }
        else
        {
            // must reset
            NEEDS_HARD_STOP = false;
            ForkedAppBooter appBooter =
                (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

            try
            {
                appBooter.stop();
            }
            catch ( AppBooterServiceException e )
            {
                Assert.fail( "The Test failed to stop a forked JVM, so, it was either (most likely) not running or an orphaned process that you will need to kill." );
            }
        }

    }

    protected File getOverridableFile( String file )
    {
        // the test can override the test config.
        File testConfigFile = this.getTestResourceAsFile( "test-config/" + file );

        // if the tests doesn't have a different config then use the default.
        // we need to replace every time to make sure no one changes it.
        if ( testConfigFile == null || !testConfigFile.exists() )
        {
            testConfigFile = this.getResource( "default-config/" + file );
        }
        else
        {
            System.out.println( "This test is using its own " + file + " " + testConfigFile );
        }
        return testConfigFile;
    }

    private void copyConfigFile( String configFile, String destShortName, Map<String, String> variables, String path )
        throws IOException
    {
        // the test can override the test config.
        File testConfigFile = this.getOverridableFile( configFile );

        System.out.println( "copying " + configFile + " to:  "
            + new File( AbstractNexusIntegrationTest.nexusBaseDir + "/" + RELATIVE_CONF_DIR, configFile ) );

        FileTestingUtils.interpolationFileCopy( testConfigFile, new File( AbstractNexusIntegrationTest.nexusBaseDir
            + "/" + ( path == null ? RELATIVE_CONF_DIR : path ), destShortName ), variables );

    }

    // Overloaded helpers
    private void copyConfigFile( String configFile )
        throws IOException
    {
        this.copyConfigFile( configFile, (String) null );
    }

    private void copyConfigFile( String configFile, String path )
        throws IOException
    {
        this.copyConfigFile( configFile, new HashMap<String, String>(), path );
    }

    private void copyConfigFile( String configFile, Map<String, String> variables )
        throws IOException
    {
        this.copyConfigFile( configFile, configFile, variables, null );

    }

    private void copyConfigFile( String configFile, Map<String, String> variables, String path )
        throws IOException
    {
        this.copyConfigFile( configFile, configFile, variables, path );

    }

    /**
     * Returns a File if it exists, null otherwise. Files returned by this method must be located in the
     * "src/test/resourcs/nexusXXX/" folder.
     *
     * @param relativePath path relative to the nexusXXX directory.
     * @return A file specified by the relativePath. or null if it does not exist.
     */
    protected File getTestResourceAsFile( String relativePath )
    {
        String resource = this.getTestId() + "/" + relativePath;
        return this.getResource( resource );
    }

    protected String getTestId()
    {
        String packageName = this.getClass().getPackage().getName();
        return packageName.substring( packageName.lastIndexOf( '.' ) + 1, packageName.length() );
    }

    /**
     * Returns a File if it exists, null otherwise. Files returned by this method must be located in the
     * "src/test/resourcs/nexusXXX/files/" folder.
     *
     * @param relativePath path relative to the files directory.
     * @return A file specified by the relativePath. or null if it does not exist.
     */
    protected File getTestFile( String relativePath )
    {
        return this.getTestResourceAsFile( "files/" + relativePath );
    }

    protected File getResource( String resource )
    {
        System.out.println( "Looking for resource: " + resource );
        URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );
        System.out.println( "found: " + classURL );

        try
        {
            return classURL == null ? null : new File( URLDecoder.decode( classURL.getFile(), "UTF-8" ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "This test assumes the use of UTF-8 encoding: " + e.getMessage(), e );
        }
    }

    /**
     * See oncePerClassSetUp.
     */
    @BeforeClass
    public static void staticOncePerClassSetUp()
    {
        // hacky state machine
        NEEDS_INIT = true;
    }

    @AfterClass
    public static void oncePerClassTearDown()
        throws Exception
    {
        // stop nexus
        new AbstractNexusIntegrationTest().stopNexus();
    }

    private void setupContainer()
    {
        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        context = new HashMap<String, Object>();

        context.put( "basedir", basedir );

        boolean hasPlexusHome = context.containsKey( "plexus.home" );

        if ( !hasPlexusHome )
        {
            File f = new File( basedir, "target/plexus-home" );

            if ( !f.isDirectory() )
            {
                f.mkdir();
            }

            context.put( "plexus.home", f.getAbsolutePath() );
        }

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration().setName( "test" ).setContext( context ).setContainerConfiguration(
                                                                                                                   getClass().getName().replace(
                                                                                                                                                 '.',
                                                                                                                                                 '/' )
                                                                                                                       + ".xml" );

        try
        {
            container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }
    }

    protected Object lookup( String componentKey )
        throws Exception
    {
        return container.lookup( componentKey );
    }

    protected Object lookup( String role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    protected String getRelitiveArtifactPath( Gav gav )
        throws FileNotFoundException
    {
        return this.getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                             gav.getExtension() );
    }

    protected String getRelitiveArtifactPath( String groupId, String artifactId, String version, String extension )
        throws FileNotFoundException
    {
        return groupId.replace( '.', '/' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "."
            + extension;
    }

    protected File downloadArtifact( Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      targetDirectory );
    }

    protected File downloadArtifact( String groupId, String artifact, String version, String type,
                                     String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( this.nexusTestRepoUrl, groupId, artifact, version, type, targetDirectory );
    }

    protected File downloadArtifactFromRepository( String repoId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( AbstractNexusIntegrationTest.baseNexusUrl + REPOSITORY_RELATIVE_URL + repoId
            + "/", gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(), targetDirectory );
    }

    protected File downloadArtifactFromGroup( String groupId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( AbstractNexusIntegrationTest.baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL
            + groupId + "/", gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      targetDirectory );
    }

    protected File downloadArtifact( String baseUrl, String groupId, String artifact, String version, String type,
                                     String targetDirectory )
        throws IOException
    {
        URL url =
            new URL( baseUrl + groupId.replace( '.', '/' ) + "/" + artifact + "/" + version + "/" + artifact + "-"
                + version + "." + type );

        return this.downloadFile( url, targetDirectory + "/" + artifact + "-" + version + "." + type );
    }

    protected File downloadFile( URL url, String targetFile )
        throws IOException
    {

        return RequestFacade.downloadFile( url, targetFile );
    }

    protected void deleteFromRepository( String groupOrArtifactPath )
        throws IOException
    {
        this.deleteFromRepository( this.testRepositoryId, groupOrArtifactPath );
    }

    protected void deleteFromRepository( String repository, String groupOrArtifactPath )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repository + "/content/" + groupOrArtifactPath;

        System.out.println( "deleting: " + serviceURI );

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );

        if ( !response.getStatus().isSuccess() )
        {
            System.out.println( "Failed to delete: " + serviceURI + "  - Status: " + response.getStatus() );
        }
    }

    public String getBaseNexusUrl()
    {
        return baseNexusUrl;
    }

    public static void setBaseNexusUrl( String baseNexusUrl )
    {
        AbstractNexusIntegrationTest.baseNexusUrl = baseNexusUrl;
    }

    public String getNexusTestRepoUrl()
    {
        return nexusTestRepoUrl;
    }

    public void setNexusTestRepoUrl( String nexusTestRepoUrl )
    {
        this.nexusTestRepoUrl = nexusTestRepoUrl;
    }

    public PlexusContainer getContainer()
    {
        return this.container;
    }

    public String getNexusBaseDir()
    {
        return nexusBaseDir;
    }

    public String getTestRepositoryId()
    {
        return testRepositoryId;
    }
    
    public String getRepositoryUrl( String repoId )
    {
        return baseNexusUrl + REPOSITORY_RELATIVE_URL + repoId + "/";
    }
    
    public String getGroupUrl( String groupId )
    {
        return baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL + groupId + "/";
    }

    protected static boolean printKnownErrorButDoNotFail( Class<? extends AbstractNexusIntegrationTest> clazz,
                                                          String... tests )
    {
        StringBuffer error =
            new StringBuffer(
                              "\n\n\n\n\n\n*********************************************************************************" );
        error.append( "*\n*\n*\n*\n*\n*" );
        error.append( "\n* This test is being skipped because its known to fail," );
        error.append( "\n* It is a very minor error, and is only a problem if you start sending in " );
        error.append( "\n* raw REST request to Nexus. (it is not a security problem)" );
        error.append( "*\n*\n" );
        error.append( "*\n* TestClass: " + clazz );
        for ( String test : tests )
        {
            error.append( "*\n* Test: " + test );
        }
        error.append( "*\n*\n*\n*\n*\n**********************************************************************************" );

        System.out.println( error.toString() );

        return true;
    }
}
