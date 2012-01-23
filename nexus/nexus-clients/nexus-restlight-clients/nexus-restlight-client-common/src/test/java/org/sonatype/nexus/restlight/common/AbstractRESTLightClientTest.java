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
package org.sonatype.nexus.restlight.common;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.slf4j.LoggerFactory;

public class AbstractRESTLightClientTest
{

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger( AbstractRESTLightClientTest.class );

    private static final String TEST_PORT_SYSPROP = "test.port";

    private Server server;

    private int port;

    @Test
    public void testGetVocabilary()
        throws Exception
    {
        AbstractRESTLightClient c = new AbstractRESTLightClient( null, null, null, null )
        {
            @Override
            protected void connect()
                throws RESTLightClientException
            {
                // do nothing
            }
        };

        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        map.put( "1.0", Arrays.asList( "a", "b" ) );
        map.put( "1.9", Arrays.asList( "a", "b", "c", "d" ) );

        List<String> voc = c.getVocabilary( map, "1.0" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b" ) );

        voc = c.getVocabilary( map, "1.1" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b" ) );

        voc = c.getVocabilary( map, "1.9" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b", "c", "d" ) );

        voc = c.getVocabilary( map, "1.10" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b", "c", "d" ) );
    }

    @Test
    public void testGetErrorHandling()
        throws Exception
    {
        serve( 400, "validation error" );

        try
        {
            client().get( "/foo" );
            client().get( "/foo", Collections.<String, Object>emptyMap() );
            client().get( "http://localhost:" + port + "/foo", Collections.<String, Object>emptyMap(), true );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

        try
        {
            client().get( "/foo", Collections.<String, Object>emptyMap() );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

        try
        {
            client().get( "http://localhost:" + port + "/foo", Collections.<String, Object>emptyMap(), true );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

    }

    @Test
    public void testPostErrorHandling()
        throws Exception
    {
        serve( 400, "validation error" );

        try
        {
            client().post( "/foo", Collections.<String, Object>emptyMap(), null );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

        try
        {
            client().postWithResponse( "/foo", Collections.<String, Object>emptyMap(), null );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }
    }

    @Test
    public void testPutErrorHandling()
        throws Exception
    {
        serve( 400, "validation error" );

        try
        {
            client().put( "/foo", Collections.<String, Object>emptyMap(), null );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

        try
        {
            client().putWithResponse( "/foo", Collections.<String, Object>emptyMap(), null );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }
    }

    @Test
    public void testDeleteErrorHandling()
        throws Exception
    {
        serve( 400, "validation error" );

        try
        {
            client().delete( "/foo", Collections.<String, Object>emptyMap() );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }

        try
        {
            client().deleteWithResponse( "/foo", Collections.<String, Object>emptyMap(), new Document() );
        }
        catch ( RESTLightClientException e )
        {
            assertThat( e.getMessage(), containsString("validation error") );
        }
    }

    private AbstractRESTLightClient client()
        throws RESTLightClientException
    {
        return new AbstractRESTLightClient( "http://localhost:" + port, "user", "pw", null )
            {
            };
    }

    private void serve( final int code, final String text )
        throws Exception
    {
        String portStr = System.getProperty( TEST_PORT_SYSPROP );

        if ( portStr != null )
        {
            port = Integer.parseInt( portStr );
            logger.info( "Using port: " + port + ", given by system property '" + TEST_PORT_SYSPROP + "'." );
        }
        else
        {
            logger.info( "Randomly looking for an open port..." );

            ServerSocket ss = new ServerSocket( 0 );
            try
            {
                port = ss.getLocalPort();
            }
            finally
            {
                ss.close();
            }
        }

        logger.info( "Starting test server on port: " + port );

        server = new Server( port );

        HandlerWrapper wrapper = new HandlerWrapper();
        wrapper.addHandler( new AbstractHandler()
        {

            @Override
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                response.setStatus( 400 );
                response.getWriter().write( text );
    (                ( Request)request).setHandled( true );
            }
        } );

        server.setHandler( wrapper );
        server.start();
    }
}
