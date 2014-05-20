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

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.SSLContextSelector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link HttpClientFactory}.
 *
 * @since 2.2
 */
@Singleton
@Named
public class HttpClientFactoryImpl
    extends ComponentSupport
    implements HttpClientFactory
{
  /**
   * Key for customizing default (and max) keep alive duration when remote server does not state anything, or states
   * some unreal high value. Value is milliseconds.
   */
  private static final String KEEP_ALIVE_MAX_DURATION_KEY = "nexus.apacheHttpClient4x.keepAliveMaxDuration";

  /**
   * Default keep alive max duration: 30 seconds.
   */
  private static final long KEEP_ALIVE_MAX_DURATION_DEFAULT = TimeUnit.SECONDS.toMillis(30);

  /**
   * Key for customizing connection pool maximum size. Value should be integer equal to 0 or greater. Pool size of 0
   * will actually prevent use of pool. Any positive number means the actual size of the pool to be created. This is
   * a
   * hard limit, connection pool will never contain more than this count of open sockets.
   */
  private static final String CONNECTION_POOL_MAX_SIZE_KEY = "nexus.apacheHttpClient4x.connectionPoolMaxSize";

  /**
   * Default pool max size: 200.
   */
  private static final int CONNECTION_POOL_MAX_SIZE_DEFAULT = 200;

  /**
   * Key for customizing connection pool size per route (usually per-repository, but not quite in case of Mirrors).
   * Value should be integer equal to 0 or greater. Pool size of 0 will actually prevent use of pool. Any positive
   * number means the actual size of the pool to be created.
   */
  private static final String CONNECTION_POOL_SIZE_KEY = "nexus.apacheHttpClient4x.connectionPoolSize";

  /**
   * Default pool size: 20.
   */
  private static final int CONNECTION_POOL_SIZE_DEFAULT = 20;

  /**
   * Key for customizing connection pool idle time. In other words, how long open connections (sockets) are kept in
   * pool idle (unused) before they get evicted and closed. Value is milliseconds.
   */
  private static final String CONNECTION_POOL_IDLE_TIME_KEY = "nexus.apacheHttpClient4x.connectionPoolIdleTime";

  /**
   * Default pool idle time: 30 seconds.
   */
  private static final long CONNECTION_POOL_IDLE_TIME_DEFAULT = TimeUnit.SECONDS.toMillis(30);

  /**
   * Key for customizing connection pool timeout. In other words, how long should a HTTP request execution be blocked
   * when pool is depleted, for a connection. Value is milliseconds.
   */
  private static final String CONNECTION_POOL_TIMEOUT_KEY = "nexus.apacheHttpClient4x.connectionPoolTimeout";

  /**
   * Default pool timeout: 30 seconds.
   */
  private static final int CONNECTION_POOL_TIMEOUT_DEFAULT = (int) TimeUnit.SECONDS.toMillis(30);

  // ==

  private final Provider<SystemStatus> systemStatusProvider;
  
  private final Provider<RemoteStorageContext> globalRemoteStorageContextProvider;

  /**
   * The low level core event bus.
   */
  private final EventBus eventBus;

  private static class ManagedClientConnectionManager
      extends PoolingHttpClientConnectionManager
  {
    public ManagedClientConnectionManager(final Registry<ConnectionSocketFactory> schemeRegistry) {
      super(schemeRegistry);
    }

    /**
     * Do nothing in order to avoid unwanted shutdown of shared connection manager.
     */
    @Override
    public void shutdown() {
      // empty
    }

    private void _shutdown() {
      super.shutdown();
    }
  }

  /**
   * Shared client connection manager.
   */
  private final ManagedClientConnectionManager sharedConnectionManager;

  /**
   * Thread evicting idle open connections from {@link #sharedConnectionManager}.
   */
  private final EvictingThread evictingThread;

  /**
   * Used to install created {@link PoolingClientConnectionManager} into jmx.
   */
  private final PoolingClientConnectionManagerMBeanInstaller jmxInstaller;

  @Inject
  public HttpClientFactoryImpl(final Provider<SystemStatus> systemStatusProvider,
                               final @Named("global") Provider<RemoteStorageContext> globalRemoteStorageContextProvider,
                               final EventBus eventBus,
                               final PoolingClientConnectionManagerMBeanInstaller jmxInstaller,
                               final List<SSLContextSelector> selectors)
  {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
    this.globalRemoteStorageContextProvider = checkNotNull(globalRemoteStorageContextProvider);
    this.jmxInstaller = checkNotNull(jmxInstaller);
    this.sharedConnectionManager = createClientConnectionManager(selectors);

    long connectedPoolIdleTime = SystemPropertiesHelper.getLong(CONNECTION_POOL_IDLE_TIME_KEY, CONNECTION_POOL_IDLE_TIME_DEFAULT);
    this.evictingThread = new EvictingThread(sharedConnectionManager, connectedPoolIdleTime);
    this.evictingThread.start();

    this.eventBus = checkNotNull(eventBus);
    this.eventBus.register(this);

    this.jmxInstaller.register(sharedConnectionManager);
  }

  private ManagedClientConnectionManager createClientConnectionManager(final List<SSLContextSelector> selectors) {
    final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", new NexusSSLConnectionSocketFactory(
            (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault(),
            SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER, selectors)
        ).build();

    final ManagedClientConnectionManager connManager = new ManagedClientConnectionManager(registry);
    final int maxConnectionCount = SystemPropertiesHelper.getInteger(CONNECTION_POOL_MAX_SIZE_KEY, CONNECTION_POOL_MAX_SIZE_DEFAULT);
    final int poolSize = SystemPropertiesHelper.getInteger(CONNECTION_POOL_SIZE_KEY, CONNECTION_POOL_SIZE_DEFAULT);
    final int perRouteConnectionCount = Math.min(poolSize, maxConnectionCount);

    connManager.setMaxTotal(maxConnectionCount);
    connManager.setDefaultMaxPerRoute(perRouteConnectionCount);

    return connManager;
  }

  /**
   * Performs a clean shutdown on this component, it kills the evicting thread and shuts down the shared connection
   * manager. Multiple invocation of this method is safe, it will not do anything.
   */
  public synchronized void shutdown() {
    evictingThread.interrupt();
    jmxInstaller.unregister();
    sharedConnectionManager._shutdown();
    eventBus.unregister(this);
    log.info("Stopped");
  }

  @Subscribe
  public void onEvent(final NexusStoppedEvent event) {
    shutdown();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      shutdown();
    }
    finally {
      super.finalize();
    }
  }

  @Override
  public HttpClient create() {
    RemoteStorageContext context = globalRemoteStorageContextProvider.get();
    Builder builder = prepare(new RemoteStorageContextCustomizer(context));

    // FIXME: Why is this here and not general?
    boolean reuseConnections = reuseConnectionsNeeded(context);
    if (!reuseConnections) {
      builder.getHttpClientBuilder().setConnectionReuseStrategy(new NoConnectionReuseStrategy());
    }

    return builder.build();
  }

  @VisibleForTesting
  boolean reuseConnectionsNeeded(final RemoteStorageContext context) {
    // return true if any of the auth is NTLM based, as NTLM must have keep-alive to work
    if (context != null) {
      if (context.getRemoteAuthenticationSettings() instanceof NtlmRemoteAuthenticationSettings) {
        return true;
      }
      if (context.getRemoteProxySettings() != null) {
        if (context.getRemoteProxySettings().getHttpProxySettings() != null &&
            context.getRemoteProxySettings().getHttpProxySettings()
                .getProxyAuthentication() instanceof NtlmRemoteAuthenticationSettings) {
          return true;
        }
        if (context.getRemoteProxySettings().getHttpsProxySettings() != null &&
            context.getRemoteProxySettings().getHttpsProxySettings()
                .getProxyAuthentication() instanceof NtlmRemoteAuthenticationSettings) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public HttpClient create(final Customizer customizer) {
    return prepare(customizer).build();
  }

  @Override
  public Builder prepare(final Customizer customizer) {
    final Builder builder = new Builder();

    // dependencies
    builder.getHttpClientBuilder().setConnectionManager(sharedConnectionManager);

    // configurable defaults
    int poolTimeout = SystemPropertiesHelper.getInteger(CONNECTION_POOL_TIMEOUT_KEY, CONNECTION_POOL_TIMEOUT_DEFAULT);
    builder.getRequestConfigBuilder().setConnectionRequestTimeout(poolTimeout);

    long keepAliveDuration = SystemPropertiesHelper.getLong(KEEP_ALIVE_MAX_DURATION_KEY, KEEP_ALIVE_MAX_DURATION_DEFAULT);
    builder.getHttpClientBuilder().setKeepAliveStrategy(new NexusConnectionKeepAliveStrategy(keepAliveDuration));

    // defaults
    builder.getConnectionConfigBuilder().setBufferSize(8 * 1024);
    builder.getRequestConfigBuilder().setCookieSpec(CookieSpecs.IGNORE_COOKIES);
    builder.getRequestConfigBuilder().setExpectContinueEnabled(false);
    builder.getRequestConfigBuilder().setStaleConnectionCheckEnabled(false);

    // Apply default user-agent
    builder.setUserAgent(getUserAgent());
    
    customizer.customize(builder);

    return builder;
  }

  private String userAgent;

  private String platformEditionShort;
  
  private synchronized String getUserAgent() {
    SystemStatus status = systemStatusProvider.get();

    // Cache platform details or re-cache if the edition has changed
    if (userAgent == null || !status.getEditionShort().equals(platformEditionShort)) {
      // track edition for cache invalidation
      platformEditionShort = status.getEditionShort();

      userAgent =
          String.format("Nexus/%s (%s; %s; %s; %s; %s)",
              status.getVersion(),
              platformEditionShort,
              System.getProperty("os.name"),
              System.getProperty("os.version"),
              System.getProperty("os.arch"),
              System.getProperty("java.version"));
    }

    return userAgent;
  }
}
