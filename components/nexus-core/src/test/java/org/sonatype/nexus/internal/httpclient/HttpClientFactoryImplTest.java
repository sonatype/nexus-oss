/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.httpclient;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteProxySettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.httpclient.RemoteStorageContextCustomizer;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.inject.util.Providers;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class HttpClientFactoryImplTest
    extends TestSupport
{
  private HttpClientFactoryImpl testSubject;

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

  @Before
  public void prepare() {
    final DefaultRemoteConnectionSettings rcs = new DefaultRemoteConnectionSettings();
    rcs.setConnectionTimeout(1234);
    when(globalRemoteStorageContext.getRemoteConnectionSettings()).thenReturn(rcs);
    when(globalRemoteStorageContext.getRemoteProxySettings()).thenReturn(remoteProxySettings);
  }

  @Test
  public void NEXUS6220_connectionReuse() {
    testSubject = new HttpClientFactoryImpl(
        Providers.of(systemStatus),
        Providers.of(globalRemoteStorageContext),
        eventBus,
        jmxInstaller,
        null);

    // nothing NTLM related present
    {
      final DefaultRemoteStorageContext drsc = new DefaultRemoteStorageContext(null);
      Assert.assertFalse("No auth-proxy set", testSubject.reuseConnectionsNeeded(drsc));
    }

    // remote auth is NTLM
    {
      final DefaultRemoteStorageContext drsc = new DefaultRemoteStorageContext(null);
      drsc.setRemoteAuthenticationSettings(new NtlmRemoteAuthenticationSettings("a", "b", "c", "d"));
      Assert.assertTrue("NTLM target auth-proxy set", testSubject.reuseConnectionsNeeded(drsc));
    }

    // HTTP proxy is NTLM
    {
      final RemoteHttpProxySettings http = new DefaultRemoteHttpProxySettings();
      http.setProxyAuthentication(new NtlmRemoteAuthenticationSettings("a", "b", "c", "d"));
      final RemoteHttpProxySettings https = new DefaultRemoteHttpProxySettings();
      when(remoteProxySettings.getHttpProxySettings()).thenReturn(http);
      when(remoteProxySettings.getHttpsProxySettings()).thenReturn(https);
      Assert.assertTrue("NTLM HTTP proxy auth-proxy set",
              testSubject.reuseConnectionsNeeded(globalRemoteStorageContext));
    }

    // HTTPS proxy is NTLM
    {
      final RemoteHttpProxySettings http = new DefaultRemoteHttpProxySettings();
      final RemoteHttpProxySettings https = new DefaultRemoteHttpProxySettings();
      https.setProxyAuthentication(new NtlmRemoteAuthenticationSettings("a", "b", "c", "d"));
      when(remoteProxySettings.getHttpProxySettings()).thenReturn(http);
      when(remoteProxySettings.getHttpsProxySettings()).thenReturn(https);
      Assert.assertTrue("NTLM HTTPS proxy auth-proxy set",
              testSubject.reuseConnectionsNeeded(globalRemoteStorageContext));
    }
  }

  @Test
  public void credentialsProviderReplaced() {
    testSubject = new HttpClientFactoryImpl(
        Providers.of(systemStatus),
        Providers.of(globalRemoteStorageContext),
        eventBus,
        jmxInstaller,
        null);

    RemoteStorageContextCustomizer customizer = new RemoteStorageContextCustomizer(globalRemoteStorageContext);
    final Builder builder = testSubject.prepare(customizer);

    final RemoteAuthenticationSettings remoteAuthenticationSettings =
        new UsernamePasswordRemoteAuthenticationSettings("user", "pass");
    customizer.applyAuthenticationConfig(builder, remoteAuthenticationSettings, null);

    final DefaultRemoteHttpProxySettings httpProxy = new DefaultRemoteHttpProxySettings();
    httpProxy.setHostname("http-proxy");
    httpProxy.setPort(8080);
    httpProxy.setProxyAuthentication(new UsernamePasswordRemoteAuthenticationSettings("http-proxy", "http-pass"));

    final DefaultRemoteHttpProxySettings httpsProxy = new DefaultRemoteHttpProxySettings();
    httpsProxy.setHostname("https-proxy");
    httpsProxy.setPort(9090);
    httpsProxy.setProxyAuthentication(new UsernamePasswordRemoteAuthenticationSettings("https-proxy", "https-pass"));

    final DefaultRemoteProxySettings remoteProxySettings = new DefaultRemoteProxySettings();
    remoteProxySettings.setHttpProxySettings(httpProxy);
    remoteProxySettings.setHttpsProxySettings(httpsProxy);

    customizer.applyProxyConfig(builder, remoteProxySettings);

    final CredentialsProvider credentialsProvider = builder.getCredentialsProvider();

    assertThat(credentialsProvider.getCredentials(AuthScope.ANY), notNullValue(Credentials.class));
    assertThat(credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName(), equalTo("user"));
    assertThat(credentialsProvider.getCredentials(AuthScope.ANY).getPassword(), equalTo("pass"));

    final AuthScope httpProxyAuthScope = new AuthScope(new HttpHost("http-proxy", 8080));
    assertThat(credentialsProvider.getCredentials(httpProxyAuthScope), notNullValue(Credentials.class));
    assertThat(credentialsProvider.getCredentials(httpProxyAuthScope).getUserPrincipal().getName(), equalTo("http-proxy"));
    assertThat(credentialsProvider.getCredentials(httpProxyAuthScope).getPassword(), equalTo("http-pass"));

    final AuthScope httpsProxyAuthScope = new AuthScope(new HttpHost("https-proxy", 9090));
    assertThat(credentialsProvider.getCredentials(httpsProxyAuthScope), notNullValue(Credentials.class));
    assertThat(credentialsProvider.getCredentials(httpsProxyAuthScope).getUserPrincipal().getName(), equalTo("https-proxy"));
    assertThat(credentialsProvider.getCredentials(httpsProxyAuthScope).getPassword(), equalTo("https-pass"));
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
