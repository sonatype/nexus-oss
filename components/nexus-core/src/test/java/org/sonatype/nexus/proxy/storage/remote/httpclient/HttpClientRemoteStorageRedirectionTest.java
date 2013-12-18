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

package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.net.URL;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.http.QueryStringBuilder;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link HttpClientRemoteStorage#shouldFollowRedirect(ProxyRepository, ResourceStoreRequest, HttpUriRequest,
 * HttpResponse, URL)} UTs.
 *
 * @since 2.8.0
 */
public class HttpClientRemoteStorageRedirectionTest
    extends TestSupport
{

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private StatusLine statusLine;

  @Mock
  private HttpResponse response;

  @Before
  public void before() {
    when(response.getStatusLine()).thenReturn(statusLine);
  }

  /**
   * {@link HttpClientRemoteStorage#CONTENT_RETRIEVAL_MARKER_KEY} present, redirects should NOT work for URLs ending
   * with slash.
   */
  @Test
  public void doNotFollowContentRedirectsToDirIndex()
      throws Exception
  {
    final HttpClientRemoteStorage underTest =
        new HttpClientRemoteStorage(mock(ApplicationStatusSource.class),
            mock(MimeSupport.class), mock(QueryStringBuilder.class), mock(HttpClientManager.class));
    final ProxyRepository proxyMock = mock(ProxyRepository.class);
    when(proxyMock.getId()).thenReturn("id");
    when(proxyMock.getRemoteStorageContext()).thenReturn(new DefaultRemoteStorageContext(null));
    final ResourceStoreRequest rsr = new ResourceStoreRequest("/foo");

    HttpGet request = new HttpGet("http://localhost/dir/fileA");
    request.getParams().setBooleanParameter(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, true);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        is((URL) null));

    // redirect to file
    request = new HttpGet("http://localhost/dir/fileA");
    request.getParams().setBooleanParameter(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, true);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://localhost/dir/fileB"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("http://localhost/dir/fileB")));

    // redirect to dir
    request = new HttpGet("http://localhost/dir");
    request.getParams().setBooleanParameter(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, true);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://localhost/dir/"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        is((URL) null));
  }

  /**
   * {@link HttpClientRemoteStorage#CONTENT_RETRIEVAL_MARKER_KEY} not present, redirects should work as default for
   * everything.
   */
  @Test
  public void doFollowNonContentRedirectsToDirIndex()
      throws Exception
  {
    final HttpClientRemoteStorage underTest =
        new HttpClientRemoteStorage(mock(ApplicationStatusSource.class),
            mock(MimeSupport.class), mock(QueryStringBuilder.class), mock(HttpClientManager.class));
    final ProxyRepository proxyMock = mock(ProxyRepository.class);
    when(proxyMock.getId()).thenReturn("id");
    when(proxyMock.getRemoteStorageContext()).thenReturn(new DefaultRemoteStorageContext(null));
    final ResourceStoreRequest rsr = new ResourceStoreRequest("/foo");

    HttpGet request = new HttpGet("http://localhost/dir/fileA");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        is((URL) null));

    // redirect to file
    request = new HttpGet("http://localhost/dir/fileA");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://localhost/dir/fileB"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("http://localhost/dir/fileB")));

    // redirect to dir
    request = new HttpGet("http://localhost/dir");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://localhost/dir/"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        is(new URL("http://localhost/dir/")));
  }

  @Test
  public void doFollowCrossSiteRedirects()
      throws Exception
  {
    final HttpClientRemoteStorage underTest =
        new HttpClientRemoteStorage(mock(ApplicationStatusSource.class),
            mock(MimeSupport.class), mock(QueryStringBuilder.class), mock(HttpClientManager.class));
    final ProxyRepository proxyMock = mock(ProxyRepository.class);
    when(proxyMock.getId()).thenReturn("id");
    when(proxyMock.getRemoteStorageContext()).thenReturn(new DefaultRemoteStorageContext(null));
    final ResourceStoreRequest rsr = new ResourceStoreRequest("/foo");

    // simple cross redirect
    HttpGet request = new HttpGet("http://hostA/dir");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://hostB/dir"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("http://hostB/dir")));

    // cross redirect to dir (failed coz NEXUS-5744)
    request = new HttpGet("http://hostA/dir/");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://hostB/dir/"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("http://hostB/dir/")));
  }

  @Test
  public void doFollowProtocolChangingRedirects()
      throws Exception
  {
    final HttpClientRemoteStorage underTest =
        new HttpClientRemoteStorage(mock(ApplicationStatusSource.class),
            mock(MimeSupport.class), mock(QueryStringBuilder.class), mock(HttpClientManager.class));
    final ProxyRepository proxyMock = mock(ProxyRepository.class);
    when(proxyMock.getId()).thenReturn("id");
    when(proxyMock.getRemoteStorageContext()).thenReturn(new DefaultRemoteStorageContext(null));
    final ResourceStoreRequest rsr = new ResourceStoreRequest("/foo");

    // simple cross redirect
    HttpGet request = new HttpGet("http://hostA/dir");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "https://hostB/dir"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("https://hostB/dir")));

    // cross redirect to dir (failed coz NEXUS-5744)
    request = new HttpGet("https://hostA/dir/");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://hostB/dir/"));
    assertThat(underTest.shouldFollowRedirect(proxyMock, rsr, request, response, request.getURI().toURL()),
        equalTo(new URL("http://hostB/dir/")));
  }

}
