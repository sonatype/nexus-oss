package org.sonatype.nexus.integrationtests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.NexusStateUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;

import com.thoughtworks.xstream.XStream;

/**
 * curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data START NOTE,
 * this class is not really abstract so I can work around a the <code>@BeforeClass</code>, <code>@AfterClass</code>
 * issues, this should be refactored a little, but it might be ok, if we switch to TestNg
 */

public class AbstractNexusIntegrationTest
{

    public static final String REPO_TEST_HARNESS_REPO = "nexus-test-harness-repo";

    public static final String REPO_TEST_HARNESS_REPO2 = "nexus-test-harness-repo2";

    public static final String REPO_TEST_HARNESS_RELEASE_REPO = "nexus-test-harness-release-repo";

    public static final String REPO_TEST_HARNESS_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public static final String REPO_RELEASE_PROXY_REPO1 = "release-proxy-repo-1";

    public static final String REPO_TEST_HARNESS_SHADOW = "nexus-test-harness-shadow";

    protected PlexusContainer container;

    private Map<String, Object> context;

    private String basedir;

    private static boolean NEEDS_INIT = false;

    private static boolean NEEDS_HARD_STOP = false;

    public static final String REPOSITORY_RELATIVE_URL = "content/repositories/";

    public static final String GROUP_REPOSITORY_RELATIVE_URL = "content/groups/";

    public String testRepositoryId;

    public static String nexusBaseDir;

    protected static String baseNexusUrl;

    protected static String nexusWorkDir;

    protected static String nexusLogDir;

    protected static Logger log = Logger.getLogger( AbstractNexusIntegrationTest.class );

    /**
     * Flag that says if we should verify the config before startup, we do not want to do this for upgrade tests.
     */
    private boolean verifyNexusConfigBeforeStart = true;

    static
    {
        nexusBaseDir = TestProperties.getString( "nexus.base.dir" );
        baseNexusUrl = TestProperties.getString( "nexus.base.url" );
        nexusWorkDir = TestProperties.getString( "nexus.work.dir" );
        nexusLogDir = TestProperties.getString( "nexus.log.dir" );
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
        // this.nexusTestRepoUrl = baseNexusUrl + REPOSITORY_RELATIVE_URL + testRepositoryId + "/";

        // configure the logging
        SLF4JBridgeHandler.install();

    }

    /**
     * To me this seems like a bad hack around this problem. I don't have any other thoughts though. <BR/>
     * If you see this and think: "Wow, why did he to that instead of XYZ, please let me know." <BR/>
     * The issue is that we want to init the tests once (to start/stop the app) and the <code>@BeforeClass</code> is
     * static, so we don't have access to the package name of the running tests. We are going to use the package name to
     * find resources for additional setup. NOTE: With this setup running multiple Test at the same time is not
     * possible.
     *
     * @throws Exception
     */
    @Before
    public void oncePerClassSetUp()
        throws Exception
    {
        synchronized ( AbstractNexusIntegrationTest.class )
        {
            log.debug( "oncePerClassSetUp is init: " + NEEDS_INIT );
            if ( NEEDS_INIT )
            {
                // tell the console what we are doing, now that there is no output its
                log.info( "Running Test: " + this.getClass().getSimpleName() );

                HashMap<String, String> variables = new HashMap<String, String>();
                variables.put( "test-harness-id", this.getTestId() );

                // clean common work dir
                // this.cleanWorkDir();

                this.copyConfigFiles();

                if ( TestContainer.getInstance().getTestContext().isSecureTest()
                    || Boolean.valueOf( System.getProperty( "secure.test" ) ) )
                {
                    NexusConfigUtil.enableSecurity( true );
                }

                // we need to make sure the config is valid, so we don't need to hunt through log files
                if ( this.verifyNexusConfigBeforeStart )
                {
                    NexusConfigUtil.validateConfig();
                }

                // start nexus
                this.startNexus();

                // deploy artifacts
                this.deployArtifacts();

                runOnce();

                // TODO: we can remove this now that we have the soft restart
                NEEDS_INIT = false;
            }
        }
    }
    
    protected void copyConfigFiles() throws IOException
    {
        this.copyConfigFile( "nexus.xml", RELATIVE_WORK_CONF_DIR );

        // copy security config
        this.copyConfigFile( "security.xml", RELATIVE_WORK_CONF_DIR );

        // this.copyConfigFile( "log4j.properties", variables );
    }

    protected void runOnce()
        throws Exception
    {
        // must override
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
        throws Exception
    {
        // test the test directory
        File projectsDir = this.getTestResourceAsFile( "projects" );
        log.debug( "projectsDir: " + projectsDir );

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
                String deployUrl = model.getDistributionManagement().getRepository().getUrl();

                // FIXME, this needs to be fluffed up a little, should add the classifier, etc.
                String artifactFileName = model.getArtifactId() + "." + model.getPackaging();
                File artifactFile = new File( project, artifactFileName );

                log.debug( "wow, this is working: " + artifactFile.getName() );

                Gav gav =
                    new Gav( model.getGroupId(), model.getArtifactId(), model.getVersion(), null, model.getPackaging(),
                             0, new Date().getTime(), model.getName(), false, false, null, false, null );

                // the Restlet Client does not support multipart forms:
                // http://restlet.tigris.org/issues/show_bug.cgi?id=71

                // int status = DeployUtils.deployUsingPomWithRest( deployUrl, repositoryId, gav, artifactFile, pom );

                if ( !artifactFile.isFile() )
                {
                    throw new FileNotFoundException( "File " + artifactFile.getAbsolutePath() + " doesn't exists!" );
                }

                DeployUtils.deployWithWagon( this.container, "http", deployUrl, artifactFile,
                                             this.getRelitiveArtifactPath( gav ) );
                DeployUtils.deployWithWagon( this.container, "http", deployUrl, pom, this.getRelitivePomPath( gav ) );

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

        log.info( "starting nexus" );

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

            log.info( "***************************" );
            log.info( "*\n*" );
            log.info( "*  DOING A HARD START OF NEXUS." );
            log.info( "*  If your not running a single test manually, then something bad happened" );
            log.info( "*\n*" );
            log.info( "***************************" );

			NexusStateUtil.doHardStart();
        }
    }

    private void stopNexus()
        throws Exception
    {
        log.info( "stopping Nexus" );

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

            NexusStateUtil.doHardStop();
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
            log.debug( "This test is using its own " + file + " " + testConfigFile );
        }
        return testConfigFile;
    }

    protected void copyConfigFile( String configFile, String destShortName, Map<String, String> variables, String path )
        throws IOException
    {
        // the test can override the test config.
        File testConfigFile = this.getOverridableFile( configFile );

        log.debug( "copying "
            + configFile
            + " to:  "
            + new File( AbstractNexusIntegrationTest.nexusBaseDir + "/" + ( path == null ? RELATIVE_CONF_DIR : path ),
                        configFile ) );

        FileTestingUtils.interpolationFileCopy( testConfigFile, new File( AbstractNexusIntegrationTest.nexusBaseDir
            + "/" + ( path == null ? RELATIVE_CONF_DIR : path ), destShortName ), variables );

    }

    // Overloaded helpers

    protected void copyConfigFile( String configFile, String path )
        throws IOException
    {
        this.copyConfigFile( configFile, new HashMap<String, String>(), path );
    }

    protected void copyConfigFile( String configFile, Map<String, String> variables, String path )
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

    public static File getResource( String resource )
    {
        log.debug( "Looking for resource: " + resource );
        // URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );

        File rootDir = new File( TestProperties.getString( "test.root.dir" ) );
        File file = new File( rootDir, resource );

        if ( !file.exists() )
        {
            return null;
        }

        log.debug( "found: " + file );

        return file;
    }

    /**
     * See oncePerClassSetUp.
     */
    @BeforeClass
    public static void staticOncePerClassSetUp()
    {
        log.debug( "staticOncePerClassSetUp" );
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

    protected void setupContainer()
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

    protected String getRelitivePomPath( Gav gav )
        throws FileNotFoundException
    {
        return this.getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), "pom", null );
    }

    protected String getRelitiveArtifactPath( Gav gav )
        throws FileNotFoundException
    {
        return this.getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                             gav.getExtension(), gav.getClassifier() );
    }

    protected String getRelitiveArtifactPath( String groupId, String artifactId, String version, String extension,
                                              String classifier )
        throws FileNotFoundException
    {
        String classifierPart = StringUtils.isEmpty( classifier ) ? "" : "-" + classifier;
        return groupId.replace( '.', '/' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
            + classifierPart + "." + extension;
    }

    protected File downloadSnapshotArtifact( String repository, Gav gav, File parentDir )
        throws IOException
    {
        // @see http://issues.sonatype.org/browse/NEXUS-599
        // r=<repoId> -- mandatory
        // g=<groupId> -- mandatory
        // a=<artifactId> -- mandatory
        // v=<version> -- mandatory
        // c=<classifier> -- optional
        // p=<packaging> -- optional, jar is taken as default
        // http://localhost:8087/nexus/service/local/artifact/maven/redirect?r=tasks-snapshot-repo&g=nexus&a=artifact&
        // v=1.0-SNAPSHOT
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + repository + "&g=" + gav.getGroupId() + "&a="
                + gav.getArtifactId() + "&v=" + gav.getVersion();
        Response response = RequestFacade.doGetRequest( serviceURI );
        Status status = response.getStatus();
        Assert.assertEquals( "Snapshot download should redirect to a new file\n "
            + response.getRequest().getResourceRef().toString() + " \n Error: " + status.getDescription(), 301,
                             status.getCode() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString(), redirectRef );

        serviceURI = redirectRef.toString();

        File file = FileUtils.createTempFile( gav.getArtifactId(), '.' + gav.getExtension(), parentDir );
        RequestFacade.downloadFile( new URL( serviceURI ), file.getAbsolutePath() );

        return file;
    }

    protected File downloadArtifact( Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      gav.getClassifier(), targetDirectory );
    }

    protected File downloadArtifact( String groupId, String artifact, String version, String type, String classifier,
                                     String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( this.getNexusTestRepoUrl(), groupId, artifact, version, type, classifier,
                                      targetDirectory );
    }

    protected File downloadArtifactFromRepository( String repoId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( AbstractNexusIntegrationTest.baseNexusUrl + REPOSITORY_RELATIVE_URL + repoId
            + "/", gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(), gav.getClassifier(),
                                      targetDirectory );
    }

    protected File downloadArtifactFromGroup( String groupId, Gav gav, String targetDirectory )
        throws IOException
    {
        return this.downloadArtifact( AbstractNexusIntegrationTest.baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL
            + groupId + "/", gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                      gav.getClassifier(), targetDirectory );
    }

    protected File downloadArtifact( String baseUrl, String groupId, String artifact, String version, String type,
                                     String classifier, String targetDirectory )
        throws IOException
    {
        URL url = new URL( baseUrl + this.getRelitiveArtifactPath( groupId, artifact, version, type, classifier ) );

        String classifierPart = ( classifier != null ) ? "-" + classifier : "";
        return this.downloadFile( url, targetDirectory + "/" + artifact + "-" + version + classifierPart + "." + type );
    }

    protected File downloadFile( URL url, String targetFile )
        throws IOException
    {

        return RequestFacade.downloadFile( url, targetFile );
    }

    protected boolean deleteFromRepository( String groupOrArtifactPath )
        throws IOException
    {
        return this.deleteFromRepository( this.testRepositoryId, groupOrArtifactPath );
    }

    protected boolean deleteFromRepository( String repository, String groupOrArtifactPath )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repository + "/content/" + groupOrArtifactPath;

        log.debug( "deleting: " + serviceURI );

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );

        boolean deleted = response.getStatus().isSuccess();

        if ( !deleted )
        {
            log.debug( "Failed to delete: " + serviceURI + "  - Status: " + response.getStatus() );
        }

        // fake it because the artifact doesn't exist
        // TODO: clean this up.
        if ( response.getStatus().getCode() == 404 )
        {
            deleted = true;
        }

        return deleted;
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
        return baseNexusUrl + REPOSITORY_RELATIVE_URL + testRepositoryId + "/";
    }

    public String getNexusTestRepoServiceUrl()
    {
        return baseNexusUrl + "service/local/repositories/" + testRepositoryId + "/content/";
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

    public void setTestRepositoryId( String repoId )
    {
        this.testRepositoryId = repoId;
    }

    public String getRepositoryUrl( String repoId )
    {
        return baseNexusUrl + REPOSITORY_RELATIVE_URL + repoId + "/";
    }

    public String getGroupUrl( String groupId )
    {
        return baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL + groupId + "/";
    }

    protected boolean isVerifyNexusConfigBeforeStart()
    {
        return verifyNexusConfigBeforeStart;
    }

    protected void setVerifyNexusConfigBeforeStart( boolean verifyNexusConfigBeforeStart )
    {
        this.verifyNexusConfigBeforeStart = verifyNexusConfigBeforeStart;
    }

    protected boolean printKnownErrorButDoNotFail( Class<? extends AbstractNexusIntegrationTest> clazz, String... tests )
    {
        StringBuffer error =
            new StringBuffer(
                              "*********************************************************************************" );
        error.append( "\n* This test is being skipped because its known to fail," );
        error.append( "\n* It is a very minor error, and is only a problem if you start sending in " );
        error.append( "\n* raw REST request to Nexus. (it is not a security problem)" );
        error.append( "*\n*\n" );
        error.append( "*\n* TestClass: " + clazz );
        for ( String test : tests )
        {
            error.append( "*\n* Test: " + test );
        }
        error.append( "\n**********************************************************************************" );

        System.out.println( error.toString() );

        return true;
    }

    public XStream getXMLXStream()
    {
        return XStreamFactory.getXmlXStream();
    }

    public XStream getJsonXStream()
    {
        return XStreamFactory.getJsonXStream();
    }

}
