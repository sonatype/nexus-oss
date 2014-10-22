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
package org.sonatype.nexus.internal.httpclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.httpclient.RemoteStorageContextCustomizer;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

/**
 * UT ensuring that User-Agent header is set for all the HTTP scenarios, from plain method invocations, thru
 * proxy connection and event tunneled proxy connections using CONNECT on behalf of the client. Related to
 * change done for NEXUS-7575: User agent not set on HC4 tunneled connections.
 *
 * @since 3.0
 */
public class HttpClientFactoryImplRemoteTest
    extends TestSupport
{
  private HttpClientFactoryImpl testSubject;

  private UserAgentChecker userAgentChecker;

  private Server server;

  private int port;

  @Mock
  private EventBus eventBus;

  @Mock
  private SystemStatus systemStatus;

  @Mock
  private RemoteStorageContext globalRemoteStorageContext;

  @Mock
  private RemoteProxySettings remoteProxySettings;

  @Mock
  private RemoteHttpProxySettings remoteHttpProxySettings;

  @Mock
  private PoolingClientConnectionManagerMBeanInstaller jmxInstaller;

  @Before
  public void prepare() throws Exception {
    userAgentChecker = new UserAgentChecker();
    try (final ServerSocket ss = new ServerSocket(0)) {
      port = ss.getLocalPort();
    }
    server = new Server(port);
    server.setHandler(userAgentChecker);
    server.start();

    when(systemStatus.getEditionShort()).thenReturn("OSS");
    when(systemStatus.getVersion()).thenReturn("3.0");

    final DefaultRemoteConnectionSettings rcs = new DefaultRemoteConnectionSettings();
    rcs.setConnectionTimeout(1234);
    when(globalRemoteStorageContext.getRemoteConnectionSettings()).thenReturn(rcs);
    when(globalRemoteStorageContext.getRemoteProxySettings()).thenReturn(remoteProxySettings);
    when(remoteProxySettings.getHttpProxySettings()).thenReturn(remoteHttpProxySettings);
    when(remoteProxySettings.getHttpsProxySettings()).thenReturn(remoteHttpProxySettings);

    // jetty acts as proxy
    when(remoteHttpProxySettings.getHostname()).thenReturn("localhost");
    when(remoteHttpProxySettings.getPort()).thenReturn(port);
  }

  protected void userAgentCheck(boolean useProxy, ClientActivity activity) throws Exception {
    // jetty acts as proxy if needed
    when(remoteHttpProxySettings.isEnabled()).thenReturn(useProxy);
    try {
      testSubject = new HttpClientFactoryImpl(
          Providers.of(systemStatus),
          Providers.of(globalRemoteStorageContext),
          eventBus,
          jmxInstaller,
          null);

      // just to grab UA
      final Builder builder = testSubject.prepare(new RemoteStorageContextCustomizer(globalRemoteStorageContext));
      final String userAgent = builder.getUserAgent();
      final HttpClient client = builder.build();

      activity.perform(client);

      assertThat(userAgentChecker.getUserAgents(), hasSize(1)); // same UA should be used even if multiple reqs
      assertThat(userAgentChecker.getUserAgents().iterator().next(), equalTo(userAgent)); // the one we set must be used
    }
    finally {
      testSubject.shutdown();
    }
  }

  // ==

  @Test
  public void tunneledProxy() throws Exception {
    // HTTP proxy HTTPS target
    userAgentCheck(true, new ClientActivity()
    {
      @Override
      public void perform(final HttpClient client) throws Exception {
        final HttpGet get = new HttpGet("https://www.somehost.com/");
        client.execute(get); // not interested in result
      }
    });
  }

  @Test
  public void plainProxy() throws Exception {
    // HTTP proxy HTTP target
    userAgentCheck(true, new ClientActivity()
    {
      @Override
      public void perform(final HttpClient client) throws Exception {
        final HttpGet get = new HttpGet("http://www.somehost.com/");
        client.execute(get); // not interested in result
      }
    });
  }

  @Test
  public void get() throws Exception {
    // no HTTP proxy HTTP target
    userAgentCheck(false, new ClientActivity()
    {
      @Override
      public void perform(final HttpClient client) throws Exception {
        final HttpGet get = new HttpGet("http://localhost:" + port);
        client.execute(get); // not interested in result
      }
    });
  }

  @Test
  public void post() throws Exception {
    // no HTTP proxy HTTP target
    userAgentCheck(false, new ClientActivity()
    {
      @Override
      public void perform(final HttpClient client) throws Exception {
        final HttpPost post = new HttpPost("http://localhost:" + port);
        client.execute(post); // not interested in result
      }
    });
  }


  // ==

  private static interface ClientActivity
  {
    void perform(HttpClient client) throws Exception;
  }

  private static class UserAgentChecker
      extends AbstractHandler
  {
    public static final String NO_AGENT = "";

    private final Set<String> userAgents = Sets.newHashSet();

    public Set<String> getUserAgents() {
      return userAgents;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
                       final HttpServletResponse response)
        throws IOException, ServletException
    {
      final String ua = request.getHeader("user-agent");
      if (Strings.isNullOrEmpty(ua)) {
        userAgents.add(NO_AGENT);
      }
      else {
        userAgents.add(ua);
      }
    }
  }
}
