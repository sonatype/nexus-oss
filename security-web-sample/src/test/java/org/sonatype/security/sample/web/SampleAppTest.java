package org.sonatype.security.sample.web;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ServletServer;

public class SampleAppTest
    extends PlexusTestCase
{
    private static String CONF_DIR = "target/plexus-work/conf";

    private ServletServer server;

    public void testAdminRequests()
        throws Exception
    {
        Response response = this.doGet( "sample/test", "admin", "admin123" );
        String responseText = ( response.getEntity() != null ) ? response.getEntity().getText() : "";
        Assert.assertTrue( "Response: "+ response.getStatus() +"\n" + responseText + "\nredirect: "+ response.getLocationRef(), response.getStatus().isSuccess() );
        
        
        response = this.doGet( "sample/test/", "admin", "admin123" );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );

        response = this.doGet( "sample/test", null, null );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );
        
        response = this.doGet( "sample/test/", null, null );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );

        response = this.doGet( "sample/test", "admin", "wrong-password" );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );
        
        response = this.doGet( "sample/test/", "admin", "wrong-password" );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );
    }

    public void testUserRequests()
        throws Exception
    {
        Response response = this.doGet( "sample/test", "test-user", "deployment123" );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );

        response = this.doGet( "sample/test", null, null );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );

        response = this.doGet( "sample/test", "test-user", "wrong-password" );
        Assert.assertEquals( "Response: " + response.getEntity().getText(), 401, response.getStatus().getCode() );
    }

    private Response doGet( String urlPart, String username, String password )
    {
        Client restClient = new Client( new org.restlet.Context(), Protocol.HTTP );

        Request request = new Request();
        request.setResourceRef( server.getUrl( urlPart ) );
        request.setMethod( Method.GET );

        if ( StringUtils.isNotEmpty( username ) )
        {
            ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
            ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );
            request.setChallengeResponse( authentication );
        }

        return restClient.handle( request );
    }

    @Override
    protected void customizeContext( org.codehaus.plexus.context.Context context )
    {
        super.customizeContext( context );
        context.put( "security-xml-file", CONF_DIR + "/security.xml" );
        context.put( "application-conf", CONF_DIR );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy security.xml in place
        // the test security.xml name will be <package-name>.<test-name>-security.xml
        String baseName = "target/test-classes/" + this.getClass().getName().replaceAll( "\\.", "/" );
        File securityXml = new File( baseName + "-security.xml" );
        FileUtils.copyFile( securityXml, new File( CONF_DIR, "security.xml" ) );

        File securityConfigXml = new File( baseName + "-security-configuration.xml" );
        FileUtils.copyFile( securityConfigXml, new File( CONF_DIR, "security-configuration.xml" ) );
        
        new HackServletContextListener().setPlexusContainer( this.getContainer() );

        this.server = this.lookup( ServletServer.class );
        server.start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if ( this.server != null )
        {
            this.server.stop();
        }
    }

}
