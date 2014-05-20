/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.internal.httpclient.HttpClientFactoryImpl;
import org.sonatype.nexus.internal.httpclient.PoolingClientConnectionManagerMBeanInstaller;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.inject.util.Providers;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 *
 */
public class HttpClientManagerTest
    extends TestSupport
{
  @Mock
  private ProxyRepository proxyRepository;

  @Mock
  private EventBus eventBus;

  @Mock
  private SystemStatus systemStatus;

  @Mock
  private RemoteStorageContext globalRemoteStorageContext;

  @Mock
  private RemoteProxySettings remoteProxySettings;

  @Mock
  private PoolingClientConnectionManagerMBeanInstaller jmxInstaller;

  @Mock
  private HttpResponse response;

  @Mock
  private StatusLine statusLine;

  private HttpGet request;

  private HttpClientFactory httpClientFactory;

  private HttpClientManagerImpl httpClientManager;

  @Before
  public void before() {
    final DefaultRemoteConnectionSettings rcs = new DefaultRemoteConnectionSettings();
    rcs.setConnectionTimeout(10000);
    when(globalRemoteStorageContext.getRemoteConnectionSettings()).thenReturn(rcs);
    when(globalRemoteStorageContext.getRemoteProxySettings()).thenReturn(remoteProxySettings);

    httpClientFactory = new HttpClientFactoryImpl(
        Providers.of(systemStatus),
        Providers.of(globalRemoteStorageContext),
        eventBus,
        jmxInstaller,
        null);

    when(proxyRepository.getId()).thenReturn("central");
    when(response.getStatusLine()).thenReturn(statusLine);

    httpClientManager = new HttpClientManagerImpl(httpClientFactory);
  }

  @Test
  public void doNotFollowRedirectsToDirIndex()
      throws ProtocolException
  {
    final RedirectStrategy underTest =
        httpClientManager.getProxyRepositoryRedirectStrategy(proxyRepository, globalRemoteStorageContext);
    HttpContext httpContext;

    // no location header
    request = new HttpGet("http://localhost/dir/fileA");
    httpContext = new BasicHttpContext();
    httpContext.setAttribute(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, Boolean.TRUE);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    assertThat(underTest.isRedirected(request, response, httpContext), is(false));

    // redirect to file
    request = new HttpGet("http://localhost/dir/fileA");
    httpContext = new BasicHttpContext();
    httpContext.setAttribute(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, Boolean.TRUE);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://localhost/dir/fileB"));
    assertThat(underTest.isRedirected(request, response, httpContext), is(true));

    // redirect to dir
    request = new HttpGet("http://localhost/dir");
    httpContext = new BasicHttpContext();
    httpContext.setAttribute(HttpClientRemoteStorage.CONTENT_RETRIEVAL_MARKER_KEY, Boolean.TRUE);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://localhost/dir/"));
    assertThat(underTest.isRedirected(request, response, httpContext), is(false));
  }

  @Test
  public void doFollowCrossSiteRedirects()
      throws ProtocolException
  {
    final RedirectStrategy underTest =
        httpClientManager.getProxyRepositoryRedirectStrategy(proxyRepository, globalRemoteStorageContext);

    // simple cross redirect
    request = new HttpGet("http://hostA/dir");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://hostB/dir"));
    assertThat(underTest.isRedirected(request, response, new BasicHttpContext()), is(true));

    // cross redirect to dir (failed coz NEXUS-5744)
    request = new HttpGet("http://hostA/dir/");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://hostB/dir/"));
    assertThat(underTest.isRedirected(request, response, new BasicHttpContext()), is(true));
  }
}
