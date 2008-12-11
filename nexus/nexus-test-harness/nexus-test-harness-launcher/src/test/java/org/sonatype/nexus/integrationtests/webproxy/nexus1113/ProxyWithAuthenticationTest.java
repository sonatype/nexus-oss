/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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

import junit.framework.Assert;

import org.codehaus.plexus.util.Base64;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class ProxyWithAuthenticationTest
    extends AbstractNexusWebProxyIntegrationTest
{

    @Override
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();

        Assert.assertNotNull( server );
        Assert.assertNotNull( server.getProxyServlet() );
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Test
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
    public void stopWebProxy()
        throws Exception
    {
        server.getProxyServlet().setUseAuthentication( false );
        server.getProxyServlet().setAuthentications( null );
        super.stopWebProxy();
    }

}
