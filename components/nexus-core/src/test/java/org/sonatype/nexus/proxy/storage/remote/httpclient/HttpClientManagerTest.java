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

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.apachehttpclient.Hc4ProviderImpl;
import org.sonatype.nexus.apachehttpclient.PoolingClientConnectionManagerMBeanInstaller;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
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

  // ==

  @Mock
  private ApplicationConfiguration applicationConfiguration;

  @Mock
  private UserAgentBuilder userAgentBuilder;

  @Mock
  private EventBus eventBus;

  @Mock
  private RemoteStorageContext globalRemoteStorageContext;

  @Mock
  private RemoteProxySettings remoteProxySettings;

  @Mock
  private PoolingClientConnectionManagerMBeanInstaller jmxInstaller;

  @Mock
  private HttpResponse response;

  @Mock
  private HttpContext httpContext;

  @Mock
  private StatusLine statusLine;

  private HttpGet request;

  private Hc4Provider hc4Provider;

  private HttpClientManagerImpl httpClientManager;

  @Before
  public void before() {
    final DefaultRemoteConnectionSettings rcs = new DefaultRemoteConnectionSettings();
    rcs.setConnectionTimeout(10000);
    when(globalRemoteStorageContext.getRemoteConnectionSettings()).thenReturn(rcs);
    when(globalRemoteStorageContext.getRemoteProxySettings()).thenReturn(remoteProxySettings);
    when(applicationConfiguration.getGlobalRemoteStorageContext()).thenReturn(globalRemoteStorageContext);

    hc4Provider = new Hc4ProviderImpl(applicationConfiguration, userAgentBuilder, eventBus, jmxInstaller, null);

    when(proxyRepository.getId()).thenReturn("central");
    when(response.getStatusLine()).thenReturn(statusLine);

    httpClientManager = new HttpClientManagerImpl(hc4Provider, userAgentBuilder);
  }

  @Test
  public void doNotFollowRedirectsToDirIndex()
      throws ProtocolException
  {
    final RedirectStrategy underTest =
        httpClientManager.getProxyRepositoryRedirectStrategy(proxyRepository, globalRemoteStorageContext);

    // no location header
    request = new HttpGet("http://localhost/dir/fileA");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    assertThat(underTest.isRedirected(request, response, httpContext), is(false));

    // redirect to file
    request = new HttpGet("http://localhost/dir/fileA");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(
        new BasicHeader("location", "http://localhost/dir/fileB"));
    assertThat(underTest.isRedirected(request, response, httpContext), is(true));

    // redirect to dir
    request = new HttpGet("http://localhost/dir");
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
    assertThat(underTest.isRedirected(request, response, httpContext), is(true));

    // cross redirect to dir (failed coz NEXUS-5744)
    request = new HttpGet("http://hostA/dir/");
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_MOVED_TEMPORARILY);
    when(response.getFirstHeader("location")).thenReturn(new BasicHeader("location", "http://hostB/dir/"));
    assertThat(underTest.isRedirected(request, response, httpContext), is(true));
  }

  @Test
  public void doNotHandleRetries() {
    final DefaultHttpClient client =
        (DefaultHttpClient) new HttpClientManagerImpl(hc4Provider, userAgentBuilder).create(proxyRepository,
            globalRemoteStorageContext);
    // check is all set as needed: retries should be not attempted, as it is manually handled in proxy repo
    Assert.assertTrue(
        ((DefaultHttpClient) client).getHttpRequestRetryHandler() instanceof StandardHttpRequestRetryHandler);
    Assert.assertTrue(globalRemoteStorageContext.getRemoteConnectionSettings().getRetrievalRetryCount() != 0);
    // TODO: NEXUS-5368 This is disabled on purpose for now (same in HttpClientManagerImpl!)
    //Assert.assertEquals(
    //    0,
    //    ( (StandardHttpRequestRetryHandler) ( (DefaultHttpClient) client ).getHttpRequestRetryHandler() ).getRetryCount() );
    //Assert.assertEquals(
    //    false,
    //    ( (StandardHttpRequestRetryHandler) ( (DefaultHttpClient) client ).getHttpRequestRetryHandler() ).isRequestSentRetryEnabled() );
  }
}
