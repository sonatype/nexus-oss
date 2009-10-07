package org.sonatype.nexus.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.codehaus.classworlds.Launcher;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

import com.thoughtworks.xstream.XStream;

public class SimpleTest
    extends TestCase
{
    protected MockNexusEnvironment mockNexusEnvironment;

    private File bundleRoot;

    private static final int port;

    static
    {
        ServerSocket ss;
        try
        {
            ss = new ServerSocket( 0 );
            port = ss.getLocalPort();
            ss.close();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected void setUp()
        throws Exception
    {
        bundleRoot = MockNexusEnvironment.getBundleRoot( new File( "target/nexus-ui" ) );
        tamperPlexusProperties( bundleRoot );

        MockHelper.clearMocks();

        super.setUp();

        mockNexusEnvironment = new MockNexusEnvironment( getAppBooter() );

        mockNexusEnvironment.start();
    }

    private void tamperPlexusProperties( File basedir )
        throws Exception
    {
        Collection<File> files =
            FileUtils.listFiles( basedir, new NameFileFilter( "plexus.properties" ), TrueFileFilter.TRUE );
        assertEquals( 1, files.size() );

        File pp = files.iterator().next();
        assertTrue( pp.exists() );

        Properties p = new Properties();
        InputStream in = new FileInputStream( pp );
        p.load( in );
        IOUtil.close( in );

        p.setProperty( "application-port", String.valueOf( port ) );

        OutputStream out = new FileOutputStream( pp );
        p.store( out, "" );
        IOUtil.close( out );
    }

    private PlexusAppBooter getAppBooter()
        throws Exception
    {
        System.setProperty( "basedir", bundleRoot.getAbsolutePath() );

        System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            + MockAppBooterCustomizer.class.getName() );

        File classworldsConf = new File( bundleRoot, "conf/classworlds.conf" );

        if ( !classworldsConf.isFile() )
        {
            throw new IllegalStateException( "The bundle classworlds.conf file is not found (\""
                + classworldsConf.getAbsolutePath() + "\")!" );
        }

        System.setProperty( "classworlds.conf", classworldsConf.getAbsolutePath() );

        // this is non trivial here, since we are running Nexus in _same_ JVM as tests
        // and the PlexusAppBooterJSWListener (actually theused WrapperManager in it) enforces then Nexus may be
        // started only once in same JVM!
        // So, we are _overrriding_ the in-bundle plexus app booter with the simplest one
        // since we dont need all the bells-and-whistles in Service and JSW
        // but we are still _reusing_ the whole bundle environment by tricking Classworlds Launcher

        // Launcher trick -- begin
        Launcher launcher = new Launcher();
        launcher.setSystemClassLoader( Thread.currentThread().getContextClassLoader() );
        launcher.configure( new FileInputStream( classworldsConf ) ); // launcher closes stream upon configuration
        // Launcher trick -- end

        PlexusAppBooter plexusAppBooter = new PlexusAppBooter(); // set the preconfigured world

        plexusAppBooter.setWorld( launcher.getWorld() );

        return plexusAppBooter;
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        mockNexusEnvironment.stop();

        super.tearDown();
    }

    /**
     * Here, we don't mock anything, we are relying on _real_ response from real Nexus
     * 
     * @throws Exception
     */
    public void testStatusFine()
        throws Exception
    {
        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        assertEquals( "We just started Nexus withount any tampering", 200, response.getStatus().getCode() );
    }

    /**
     * We mock the status resource to be unavailable.
     * 
     * @throws Exception
     */
    public void testStatusUnavailable()
        throws Exception
    {
        MockHelper.expect( "/status", new MockResponse( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, null ) );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        assertEquals( "The status resource should be mocked", Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                      response.getStatus().getCode() );

        MockHelper.checkAndClean();
    }

    /**
     * We mock status response.
     * 
     * @throws Exception
     */
    public void testStatusCustomContent()
        throws Exception
    {
        StatusResourceResponse mockResponse = new StatusResourceResponse();

        StatusResource data = new StatusResource();

        data.setVersion( MockNexusEnvironment.getTestNexusVersion() );

        mockResponse.setData( data );

        MockHelper.expect( "/status", new MockResponse( Status.SUCCESS_OK, mockResponse ) );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        assertEquals( 200, response.getStatus().getCode() );

        NexusApplication na =
            (NexusApplication) mockNexusEnvironment.getPlexusContainer().lookup( Application.class, "nexus" );

        XStream xmlXstream = (XStream) na.getContext().getAttributes().get( PlexusRestletApplicationBridge.XML_XSTREAM );

        StatusResourceResponse responseUnmarshalled =
            (StatusResourceResponse) xmlXstream.fromXML( response.getEntity().getText(), new StatusResourceResponse() );

        assertEquals( "Versions should match", mockResponse.getData().getVersion(),
                      responseUnmarshalled.getData().getVersion() );

        MockHelper.checkAndClean();
    }

    /**
     * Here, we don't mock anything, we are just listening the _real_ response from real Nexus
     * 
     * @throws Exception
     */
    public void testListenStatusFine()
        throws Exception
    {

        MockHelper.listen( "/status", new MockListener() );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        assertEquals( "We just started Nexus withount any tampering", 200, response.getStatus().getCode() );

        MockHelper.checkAndClean();
    }

    public void testListenChecker()
        throws Exception
    {

        MockHelper.listen( "/status", new MockListener() );

        try
        {
            MockHelper.checkAndClean();
            fail();
        }
        catch ( AssertionError e )
        {
            // expected
        }
    }

    public void testMockChecker()
        throws Exception
    {

        MockHelper.expect( "/status", new MockResponse( Status.SUCCESS_OK, null ) );

        try
        {
            MockHelper.checkAndClean();
            fail();
        }
        catch ( AssertionError e )
        {
            // expected
        }
    }
}
