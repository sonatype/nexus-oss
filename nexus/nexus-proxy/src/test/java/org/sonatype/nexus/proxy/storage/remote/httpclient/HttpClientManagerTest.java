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
import static org.mockito.Mockito.when;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.nexus.httpclient.Hc4Provider;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 *
 */
public class HttpClientManagerTest
    extends TestSupport
{
    @Mock
    private ProxyRepository proxyRepository;

    @Mock
    private RemoteStorageContext ctx;

    // ==

    @Mock
    private Hc4Provider hc4Provider;

    @Mock
    private UserAgentBuilder userAgentBuilder;

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

    private RedirectStrategy underTest;

    @Before
    public void before()
    {
        when( proxyRepository.getId() ).thenReturn( "central" );
        when( response.getStatusLine() ).thenReturn( statusLine );
        when( request.getRequestLine() ).thenReturn( requestLine );
        underTest = new HttpClientManagerImpl( hc4Provider, userAgentBuilder ).getProxyRepositoryRedirectStrategy( proxyRepository, ctx );
    }

    @Test
    public void doNotFollowRedirectsToDirIndex()
        throws ProtocolException
    {
        // no location header
        when( requestLine.getMethod() ).thenReturn( "GET" );
        assertThat( underTest.isRedirected( request, response, httpContext ), is( false ) );

        // redirect to file
        when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_MOVED_TEMPORARILY );
        when( response.getFirstHeader( "location" ) ).thenReturn(
            new BasicHeader( "location", "http://localhost/dir/file" ) );
        assertThat( underTest.isRedirected( request, response, httpContext ), is( true ) );

        // redirect to dir
        when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_MOVED_TEMPORARILY );
        when( response.getFirstHeader( "location" ) ).thenReturn( new BasicHeader( "location", "http://localhost/dir/" ) );
        assertThat( underTest.isRedirected( request, response, httpContext ), is( false ) );
    }
}
