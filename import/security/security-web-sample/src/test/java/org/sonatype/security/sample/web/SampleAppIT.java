/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.sample.web;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Future;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.util.Base64;

public class SampleAppIT
    extends TestCase
{
    private static String CONF_DIR = "target/plexus-work/conf";

    private Server appServer = null;

    private int appPort;

    private AsyncHttpClient client = new AsyncHttpClient();

    public void testAdminRequests()
        throws Exception
    {
        Response response = get( "sample/test", "admin", "admin123" );
        Assert.assertEquals( response.getStatusCode(), 200 );

        response = get( "sample/test/", "admin", "admin123" );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test", null, null );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test/", null, null );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test", "admin", "wrong-password" );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test/", "admin", "wrong-password" );
        Assert.assertEquals( response.getStatusCode(), 401 );
    }

    public void testUserRequests()
        throws Exception
    {
        Response response = get( "sample/test", "test-user", "deployment123" );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test", null, null );
        Assert.assertEquals( response.getStatusCode(), 401 );

        response = get( "sample/test", "test-user", "wrong-password" );
        Assert.assertEquals( response.getStatusCode(), 401 );
    }

    public Response get( String urlString, String username, String password )
        throws Exception
    {
        BoundRequestBuilder builder = client.prepareGet( "http://localhost:" + appPort + "/" + urlString );

        if ( username != null )
        {
            builder.addHeader( "Authorization", "Basic " + getAuth( username, password ) );
        }

        Future<Response> f = builder.execute();
        Response r = f.get();

        return r;
    }

    protected static String getAuth( String username, String password )
    {
        return Base64.encode( ( username + ":" + password ).getBytes() );
    }

    protected int getFreePort()
        throws IOException
    {
        final ServerSocket socket = new ServerSocket( 0 );
        final int result = socket.getLocalPort();
        socket.close();
        return result;
    }

    protected void setUp()
        throws Exception
    {
        // System.setProperty( "org.sonatype.inject.debug", "true" );
        appPort = getFreePort();
        // copy security.xml in place
        // the test security.xml name will be <package-name>.<test-name>-security.xml
        String baseName = "target/test-classes/" + this.getClass().getName().replaceAll( "\\.", "/" );
        File securityXml = new File( baseName + "-security.xml" );
        FileUtils.copyFile( securityXml, new File( CONF_DIR, "security.xml" ) );

        File securityConfigXml = new File( baseName + "-security-configuration.xml" );
        FileUtils.copyFile( securityConfigXml, new File( CONF_DIR, "security-configuration.xml" ) );

        // Jetty server for app
        appServer = new Server();
        SocketConnector connector = new SocketConnector();
        connector.setMaxIdleTime( 1000 * 60 * 60 );
        connector.setSoLingerTime( -1 );
        connector.setPort( appPort );
        appServer.setConnectors( new Connector[] { connector } );
        WebAppContext app = new WebAppContext();
        app.setServer( appServer );
        app.setContextPath( "/sample" );
        app.setWar( "./target/sample.war" );
        HandlerList handlers = new HandlerList();
        handlers.addHandler( app );
        appServer.setHandler( handlers );
        appServer.start();
    }

    protected void tearDown()
        throws Exception
    {
        if ( appServer != null )
        {
            appServer.stop();
        }
    }

}
