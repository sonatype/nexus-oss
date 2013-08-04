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

package org.sonatype.nexus.apachehttpclient;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class Hc4ProviderImplTest
    extends TestSupport
{
  private Hc4ProviderImpl testSubject;

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

  @Before
  public void prepare() {
    final DefaultRemoteConnectionSettings rcs = new DefaultRemoteConnectionSettings();
    rcs.setConnectionTimeout(1234);
    when(globalRemoteStorageContext.getRemoteConnectionSettings()).thenReturn(rcs);
    when(globalRemoteStorageContext.getRemoteProxySettings()).thenReturn(remoteProxySettings);
    when(applicationConfiguration.getGlobalRemoteStorageContext()).thenReturn(globalRemoteStorageContext);
  }

  @Test
  public void sharedInstanceConfigurationTest() {
    setParameters();
    try {
      testSubject = new Hc4ProviderImpl(applicationConfiguration, userAgentBuilder, eventBus, jmxInstaller, null);

      final HttpClient client = testSubject.createHttpClient();
      // Note: shared instance is shared across Nexus instance. It does not features connection pooling as
      // connections are
      // never reused intentionally

      // shared client does not reuse connections (no pool)
      Assert.assertTrue(((DefaultHttpClient) client).getConnectionReuseStrategy() instanceof NoConnectionReuseStrategy);
      Assert.assertTrue(((DefaultHttpClient) client).getConnectionManager() instanceof PoolingClientConnectionManager);

      // check is all set as needed: retries
      Assert.assertTrue(
          ((DefaultHttpClient) client).getHttpRequestRetryHandler() instanceof StandardHttpRequestRetryHandler);
      Assert.assertEquals(
          globalRemoteStorageContext.getRemoteConnectionSettings().getRetrievalRetryCount(),
          ((StandardHttpRequestRetryHandler) ((DefaultHttpClient) client).getHttpRequestRetryHandler())
              .getRetryCount());
      Assert.assertEquals(
          false,
          ((StandardHttpRequestRetryHandler) ((DefaultHttpClient) client).getHttpRequestRetryHandler())
              .isRequestSentRetryEnabled());

      // check is all set as needed: everything else
      Assert.assertEquals(1234L, client.getParams().getLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, 0));
      Assert.assertEquals(1234, client.getParams().getIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 0));
      Assert.assertEquals(1234, client.getParams().getIntParameter(HttpConnectionParams.SO_TIMEOUT, 0));
      Assert.assertEquals(1234, ((PoolingClientConnectionManager) client.getConnectionManager()).getMaxTotal());
      Assert.assertEquals(1234,
          ((PoolingClientConnectionManager) client.getConnectionManager()).getDefaultMaxPerRoute());
    }
    finally {
      testSubject.shutdown();
      unsetParameters();
    }
  }

  @Test
  public void createdInstanceConfigurationTest() {
    setParameters();
    try {
      testSubject = new Hc4ProviderImpl(applicationConfiguration, userAgentBuilder, eventBus, jmxInstaller, null);

      // Note: explicitly created instance (like in case of proxies), it does pool and
      // returns customized client

      // we will reuse the "global" one, but this case is treated differently anyway by Hc4Provider
      final HttpClient client =
          testSubject.createHttpClient(applicationConfiguration.getGlobalRemoteStorageContext());
      // shared client does reuse connections (does pool)
      Assert.assertTrue(
          ((DefaultHttpClient) client).getConnectionReuseStrategy() instanceof DefaultConnectionReuseStrategy);
      Assert.assertTrue(((DefaultHttpClient) client).getConnectionManager() instanceof PoolingClientConnectionManager);

      // check is all set as needed: retries
      Assert.assertTrue(
          ((DefaultHttpClient) client).getHttpRequestRetryHandler() instanceof StandardHttpRequestRetryHandler);
      Assert.assertEquals(
          globalRemoteStorageContext.getRemoteConnectionSettings().getRetrievalRetryCount(),
          ((StandardHttpRequestRetryHandler) ((DefaultHttpClient) client).getHttpRequestRetryHandler())
              .getRetryCount());
      Assert.assertEquals(
          false,
          ((StandardHttpRequestRetryHandler) ((DefaultHttpClient) client).getHttpRequestRetryHandler())
              .isRequestSentRetryEnabled());

      // check is all set as needed: everything else
      Assert.assertEquals(1234L, client.getParams().getLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, 0));
      Assert.assertEquals(1234, client.getParams().getIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 0));
      Assert.assertEquals(1234, client.getParams().getIntParameter(HttpConnectionParams.SO_TIMEOUT, 0));
      final PoolingClientConnectionManager realConnMgr =
          (PoolingClientConnectionManager) client.getConnectionManager();
      Assert.assertEquals(1234, realConnMgr.getMaxTotal());
      Assert.assertEquals(1234, realConnMgr.getDefaultMaxPerRoute());
      client.getConnectionManager().shutdown();
    }
    finally {
      testSubject.shutdown();
      unsetParameters();
    }
  }

  // ==

  protected void setParameters() {
    System.setProperty("nexus.apacheHttpClient4x.connectionPoolMaxSize", "1234");
    System.setProperty("nexus.apacheHttpClient4x.connectionPoolSize", "1234");
    System.setProperty("nexus.apacheHttpClient4x.connectionPoolKeepalive", "1234");
    System.setProperty("nexus.apacheHttpClient4x.connectionPoolTimeout", "1234");
  }

  protected void unsetParameters() {
    System.clearProperty("nexus.apacheHttpClient4x.connectionPoolMaxSize");
    System.clearProperty("nexus.apacheHttpClient4x.connectionPoolSize");
    System.clearProperty("nexus.apacheHttpClient4x.connectionPoolKeepalive");
    System.clearProperty("nexus.apacheHttpClient4x.connectionPoolTimeout");
  }
}
