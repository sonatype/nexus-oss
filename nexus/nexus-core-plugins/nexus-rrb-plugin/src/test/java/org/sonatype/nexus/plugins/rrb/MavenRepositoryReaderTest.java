package org.sonatype.nexus.plugins.rrb;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * In this test we use example repo files that placed in the test resource catalogue To access these files locally via
 * MavenRepositoryReader that requires the http-protocol we start a Jetty server
 * 
 * @author bjorne
 */
public class MavenRepositoryReaderTest
{
    MavenRepositoryReader reader; // The "class under test"

    Server server; // An embedded Jetty server

    String localUrl = "http://local"; // This URL doesn't matter for the tests

    String nameOfConnector; // This is the host:portnumber of the Jetty connector

    @Before
    public void setUp()
        throws Exception
    {
        reader = new MavenRepositoryReader();

        // Create a Jetty server with a handler that returns the content of the
        // given target (i.e. an emulated html, S3Repo, etc, file from the test
        // resources)
        Handler handler = new AbstractHandler()
        {

            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                response.setStatus( HttpServletResponse.SC_OK );
                InputStream stream = this.getClass().getResourceAsStream( target );

                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    result.append( line ).append( System.getProperty( "line.separator" ) );
                }
                response.getWriter().println( result.toString() );
                ( (Request) request ).setHandled( true );
            }
        };

        server = new Server( 0 ); // We choose an arbitrary server port
        server.setHandler( handler ); // Assign the handler of incoming requests
        server.start();

        // After starting we must find out the host:port, so we know how to
        // connect to the server in the tests
        for ( Connector connector : server.getConnectors() )
        {
            nameOfConnector = connector.getName();
            break; // We only need one connector name (and there should only be
            // one...)
        }

    }

    @After
    public void shutDown()
        throws Exception
    {
        server.stop();
    }

    /**
     * First two test of two architypical test repos
     */

    @Test( timeout = 5000 )
    public void testReadHtml()
    {
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "htmlExample" ), localUrl, null, "test" );
        assertEquals( 7, result.size() );
    }

    @Test( timeout = 5000 )
    public void testReadS3()
    {
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "s3Example" ), localUrl, null, "test" );
        assertEquals( 14, result.size() );
    }

    /**
     * Below follows a set of tests of some typical existing repos. The respectively repo's top level is stored as a file
     * in the ordinary test resource catalog. Each file has a name indicating the repo it is taken from and an extension
     * with the date it was downloaded in the format YYYYMMDD.
     */

    @Test( timeout = 5000 )
    public void testAmazon_20100118()
    {
        // Fetched from URI http://s3.amazonaws.com/maven.springframework.org
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Amazon_20100118" ), localUrl, null, "test" );
        assertEquals( 997, result.size() );
    }

    @Test( timeout = 5000 )
    public void testApache_Snapshots()
    {
        // Fetched from URI http://repository.apache.org/snapshots
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Apache_Snapshots_20100118" ), localUrl, null, "test" );
        assertEquals( 9, result.size() );
    }

    @Test( timeout = 5000 )
    public void testCodehaus_Snapshots()
    {
        // Fetched from URI http://snapshots.repository.codehaus.org/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Codehaus_Snapshots_20100118" ), localUrl, null, "test" );
        assertEquals( 3, result.size() );
    }

    @Test( timeout = 5000 )
    public void testGoogle_Caja()
    {
        // Fetched from URI http://google-caja.googlecode.com/svn/maven
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Google_Caja_20100118" ), localUrl, null, "test" );
        assertEquals( 3, result.size() );
    }

    @Test( timeout = 5000 )
    public void testGoogle_Oauth()
    {
        // Fetched from URI http://oauth.googlecode.com/svn/code/maven
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Google_Oauth_20100118" ), localUrl, null, "test" );
        assertEquals( 4, result.size() );
    }

    @Test( timeout = 5000 )
    public void testJBoss_Maven_Release_Repository()
    {
        // Fetched from URI http://repository.jboss.org/maven2/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "JBoss_Maven_Release_Repository_20100118" ), localUrl, null,
                            "test" );
        assertEquals( 201, result.size() );
    }

    @Test( timeout = 5000 )
    public void testMaven_Central()
    {
        // Fetched from URI http://repo1.maven.org/maven2
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Maven_Central_20100118" ), localUrl, null, "test" );
        assertEquals( 647, result.size() );
    }

    @Test( timeout = 5000 )
    public void testNexus_Repository_Manager()
    {
        // Fetched from URI http://repository.sonatype.org/content/groups/forge
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Nexus_Repository_Manager_20100118" ), localUrl, null, "test" );
        assertEquals( 173, result.size() );
    }

    @Test( timeout = 5000 )
    public void testEviwares_Maven_repo()
    {
        // Fetched from URI http://www.eviware.com/repository/maven2/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Eviwares_Maven_repo_20100118" ), localUrl, null, "test" );
        assertEquals( 67, result.size() );
    }

    @Test( timeout = 5000 )
    public void testjavaNet_repo()
    {
        // Fetched from URI http://download.java.net/maven/1/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "java.net_repo_20100118" ), localUrl, null, "test" );
        assertEquals( 94, result.size() );
    }

    @Test( timeout = 5000 )
    public void testCodehaus()
    {
        // Fetched from URI http://repository.codehaus.org/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Codehaus_20100118" ), localUrl, null, "test" );
        assertEquals( 5, result.size() );
    }

    @Test( timeout = 5000 )
    public void testjavaNet2()
    {
        // Fetched from URI http://download.java.net/maven/2/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "java.net2_20100118" ), localUrl, null, "test" );
        assertEquals( 57, result.size() );
    }

    @Test( timeout = 5000 )
    public void testOpenIonaCom_Releases()
    {
        // Fetched from URI http://repo.open.iona.com/maven2/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Open.iona.com_Releases_20100118" ), localUrl, null, "test" );
        assertEquals( 8, result.size() );
    }

    /*
     * @Test(timeout = 5000) public void testterracotta() { // Fetched from URI http://download.terracotta.org/maven2/
     * List<RepositoryDirectory> result = reader .extract(getURLForTestRepoResource("terracotta_20100118"), localUrl,
     * null, "test"); assertEquals(-1, result.size()); }
     */

    @Test( timeout = 5000 )
    public void testSpringsource()
    {
        // Fetched from URI http://repository.springsource.com/
        List<RepositoryDirectory> result =
            reader.extract( getURLForTestRepoResource( "Springsource_20100118" ), localUrl, null, "test" );
        assertEquals( 995, result.size() );
    }

    /**
     * Auxiliary methods
     */
    private String getURLForTestRepoResource( String resourceName )
    {
        return "http://" + nameOfConnector + "/" + resourceName;
    }

}
