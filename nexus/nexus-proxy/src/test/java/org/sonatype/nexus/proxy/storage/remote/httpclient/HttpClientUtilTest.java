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
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 *
 */
public class HttpClientUtilTest
    extends TestSupport
{

    private DefaultHttpClient underTest;

    @Mock
    private RemoteStorageContext ctx;

    @Mock
    private HttpRequest request;

    @Mock
    private HttpResponse response;

    @Mock
    private HttpContext httpContext;

    @Mock
    private StatusLine statusLine;

    @Mock
    private RequestLine requestLine;

    @Mock
    private RemoteConnectionSettings remoteConnectionSettings;

    @Mock
    private RemoteProxySettings remoteProxySettings;

    @Before
    public void before()
    {
        when( ctx.getRemoteConnectionSettings() ).thenReturn( remoteConnectionSettings );
        when( ctx.getRemoteProxySettings() ).thenReturn( remoteProxySettings );
        underTest = (DefaultHttpClient) HttpClientUtil.configure( "ctx", ctx, logger );

        when( response.getStatusLine() ).thenReturn( statusLine );

        when( request.getRequestLine() ).thenReturn( requestLine );
    }

    @Test
    public void testRedirectIndexPage()
        throws ProtocolException
    {
        when( requestLine.getMethod() ).thenReturn( "GET" );

        final RedirectStrategy redirectStrategy = underTest.getRedirectStrategy();

        // no location header
        assertThat( redirectStrategy.isRedirected( request, response, httpContext ), is( false ) );

        when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_MOVED_TEMPORARILY );

        // redirect to file
        when( response.getFirstHeader( "location" ) ).thenReturn(
            new BasicHeader( "location", "http://localhost/dir/file" ) );
        assertThat( redirectStrategy.isRedirected( request, response, httpContext ), is( true ) );

        // redirect to dir
        when( response.getFirstHeader( "location" ) ).thenReturn(
            new BasicHeader( "location", "http://localhost/dir/" ) );
        assertThat( redirectStrategy.isRedirected( request, response, httpContext ), is( false ) );
    }


}
