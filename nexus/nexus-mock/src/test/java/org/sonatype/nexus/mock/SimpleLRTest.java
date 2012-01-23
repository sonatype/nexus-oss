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
package org.sonatype.nexus.mock;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

import com.thoughtworks.xstream.XStream;

/**
 * This test is simply wrong, so disabled for now.
 * 
 * @author cstamas
 *
 */
// This is an IT just because it runs longer then 15 seconds
public class SimpleLRTest
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

    @Before
    public void setUp()
        throws Exception
    {
        bundleRoot = MockNexusEnvironment.getBundleRoot( new File( "target/nexus-ui" ) );

        MockHelper.clearMocks();

        mockNexusEnvironment = new MockNexusEnvironment( bundleRoot.getAbsoluteFile(), port );

        mockNexusEnvironment.start();
    }

    @After
    public void tearDown()
        throws Exception
    {
        mockNexusEnvironment.stop();
        
        mockNexusEnvironment = null;
        
        Thread.yield();
        
        System.gc();
    }

    /**
     * Here, we don't mock anything, we are relying on _real_ response from real Nexus
     * 
     * @throws Exception
     */
    @Test
    public void testStatusFine()
        throws Exception
    {
        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        Assert.assertEquals( "We just started Nexus withount any tampering", 200, response.getStatus().getCode() );
    }

    /**
     * We mock the status resource to be unavailable.
     * 
     * @throws Exception
     */
    @Test
    public void testStatusUnavailable()
        throws Exception
    {
        MockHelper.expect( "/status", new MockResponse( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, null ) );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        Assert.assertEquals( "The status resource should be mocked", Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
            response.getStatus().getCode() );

        MockHelper.checkAndClean();
    }

    /**
     * We mock status response.
     * 
     * @throws Exception
     */
    @Test
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

        // Assert.assertEquals( 200, response.getStatus().getCode() );

        NexusApplication na =
            (NexusApplication) mockNexusEnvironment.getPlexusContainer().lookup( Application.class, "nexus" );

        XStream xmlXstream = (XStream) na.getContext().getAttributes().get( PlexusRestletApplicationBridge.XML_XSTREAM );

        StatusResourceResponse responseUnmarshalled =
            (StatusResourceResponse) xmlXstream.fromXML( response.getEntity().getText(), new StatusResourceResponse() );

        Assert.assertEquals( "Versions should match", mockResponse.getData().getVersion(),
            responseUnmarshalled.getData().getVersion() );

        MockHelper.checkAndClean();
    }

    /**
     * Here, we don't mock anything, we are just listening the _real_ response from real Nexus
     * 
     * @throws Exception
     */
    @Test
    public void testListenStatusFine()
        throws Exception
    {
        MockHelper.listen( "/status", new MockListener() );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:" + port + "/nexus/service/local/status" ) );

        Assert.assertEquals( "We just started Nexus withount any tampering", 200, response.getStatus().getCode() );

        MockHelper.checkAndClean();
    }

    @Test
    public void testListenChecker()
        throws Exception
    {

        MockHelper.listen( "/status", new MockListener() );

        try
        {
            MockHelper.checkAndClean();
            Assert.fail();
        }
        catch ( AssertionError e )
        {
            // expected
        }
    }

    @Test
    public void testMockChecker()
        throws Exception
    {

        MockHelper.expect( "/status", new MockResponse( Status.SUCCESS_OK, null ) );

        try
        {
            MockHelper.checkAndClean();
            Assert.fail();
        }
        catch ( AssertionError e )
        {
            // expected
        }
    }
}
