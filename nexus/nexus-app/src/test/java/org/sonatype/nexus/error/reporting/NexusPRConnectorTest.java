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
package org.sonatype.nexus.error.reporting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 * Tests for assumptions in the PR connector:
 * <ul>
 *     <li>same underlying client instance</li>
 *     <li>properly shut down on #dispose()</li>
 *     <li>(re-)configuring proxy with auth</li>
 * </ul>
 */
public class NexusPRConnectorTest
{

    @Mock
    private NexusConfiguration config;

    @Mock
    private UserAgentBuilder uaBuilder;

    @Mock
    private RemoteStorageContext ctx;

    @Mock
    private RemoteConnectionSettings remoteConnectionsSettings;

    @Mock
    private RemoteProxySettings proxySettings;

    @Mock
    private UsernamePasswordRemoteAuthenticationSettings proxyAuth;

    private NexusPRConnector underTest;

    @Before
    public void init()
    {
        MockitoAnnotations.initMocks( this );

        when( config.getGlobalRemoteStorageContext() ).thenReturn( ctx );
        when( ctx.getRemoteConnectionSettings() ).thenReturn( remoteConnectionsSettings );
        when( ctx.getRemoteProxySettings() ).thenReturn( proxySettings );

        when( remoteConnectionsSettings.getConnectionTimeout() ).thenReturn( 1234 );

        underTest = new NexusPRConnector( config, uaBuilder );
    }

    @After
    public void shutdown()
    {
        underTest.dispose();
    }

    @Test
    public void testCaching()
    {
        assertThat( underTest.client(), Matchers.equalTo( underTest.client() ) );
    }

    @Test
    public void testProxyEnabled()
    {
        final String host = "host";
        final int port = 1234;

        when( proxySettings.isEnabled() ).thenReturn( true );
        when( proxySettings.getHostname() ).thenReturn( host );
        when( proxySettings.getPort() ).thenReturn( port );

        assertThat( ConnRouteParams.getDefaultProxy( underTest.client().getParams() ), notNullValue() );
    }

    @Test
    public void testProxyAuth()
    {
        final String host = "host";
        final int port = 1234;

        when( proxySettings.isEnabled() ).thenReturn( true );
        when( proxySettings.getHostname() ).thenReturn( host );
        when( proxySettings.getPort() ).thenReturn( port );

        when( proxySettings.getProxyAuthentication() ).thenReturn( proxyAuth );
        when( proxyAuth.getUsername() ).thenReturn( "user" );
        when( proxyAuth.getPassword() ).thenReturn( "pass" );

        final HttpClient client = underTest.client();
        final HttpParams params = client.getParams();
        
        assertThat( ConnRouteParams.getDefaultProxy( params ), notNullValue() );
        assertThat( params.getParameter( AuthPNames.PROXY_AUTH_PREF ), notNullValue() );

        assertThat( ( (DefaultHttpClient) client ).getCredentialsProvider().getCredentials(
            new AuthScope( host, port ) ), notNullValue()
        );
    }
    
    @Test
    public void testProxyReconfigure()
    {
        final String host = "host";
        final int port = 1234;

        when( proxySettings.isEnabled() ).thenReturn( true );
        when( proxySettings.getHostname() ).thenReturn( host );
        when( proxySettings.getPort() ).thenReturn( port );
        
        DefaultHttpClient client = (DefaultHttpClient) underTest.client();
        HttpParams params = client.getParams();

        assertThat( ConnRouteParams.getDefaultProxy( params ), notNullValue() );
        assertThat( params.getParameter( AuthPNames.PROXY_AUTH_PREF ), nullValue() );

        assertThat( 
            client.getCredentialsProvider().getCredentials( new AuthScope( host, port ) ),
            nullValue()
        );

        when( proxySettings.getProxyAuthentication() ).thenReturn( proxyAuth );
        when( proxyAuth.getUsername() ).thenReturn( "user" );
        when( proxyAuth.getPassword() ).thenReturn( "pass" );
        
        final DefaultHttpClient reconfigured = (DefaultHttpClient) underTest.client();
        params = reconfigured.getParams();
        
        assertThat( reconfigured, equalTo( client ) );

        assertThat( ConnRouteParams.getDefaultProxy( params ), notNullValue() );
        assertThat( params.getParameter( AuthPNames.PROXY_AUTH_PREF ), notNullValue() );

        assertThat( reconfigured.getCredentialsProvider().getCredentials(
            new AuthScope( host, port ) ), notNullValue()
        );
    }

    @Test( expected = IllegalStateException.class )
    public void testDispose()
        throws ConnectionPoolTimeoutException, InterruptedException
    {
        final DefaultHttpClient client = (DefaultHttpClient) underTest.client();
        underTest.dispose();
        client.getConnectionManager().requestConnection( new HttpRoute( new HttpHost( "host" ) ), null ).getConnection(
            1000, TimeUnit.SECONDS );
    }

}
