/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.webproxy.nexus1113;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.codehaus.plexus.util.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sonatype.nexus.integrationtests.ITGroups.PROXY;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class ProxyWithAuthenticationIT
    extends AbstractNexusWebProxyIntegrationTest
{

    @Override
    @Before
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();

        Assert.assertNotNull( server );
        Assert.assertNotNull( server.getProxyServlet() );
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Test @Category(PROXY.class)
    public void validUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        byte[] encodedUserPwd = Base64.encodeBase64( "admin:123".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + new String( encodedUserPwd ) );
        con.getInputStream();

        for ( int i = 0; i < 100; i++ )
        {
            Thread.sleep( 200 );

            List<String> uris = server.getAccessedUris();
            for ( String uri : uris )
            {
                if ( uri.contains( "google.com" ) )
                {
                    return;
                }
            }
        }

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Test( expected = IOException.class )
    @Category( PROXY.class )
    public void invalidUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        byte[] encodedUserPwd = Base64.encodeBase64( "admin:1234".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + new String( encodedUserPwd ) );
        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Test( expected = IOException.class )
    @Category( PROXY.class )
    public void withoutUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        URLConnection con = url.openConnection( p );

        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Override
    @After
    public void stopWebProxy()
        throws Exception
    {
        if ( server != null )
        {
            server.getProxyServlet().setUseAuthentication( false );
            server.getProxyServlet().setAuthentications( null );
        }
        super.stopWebProxy();
    }

}
