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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Preconditions;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

/**
 * Default implementation of {@link HttpClientManager}.
 *
 * @author cstamas
 * @since 2.2
 */
@Singleton
@Named
public class HttpClientManagerImpl
    extends ComponentSupport
    implements HttpClientManager
{
  private final Hc4Provider hc4Provider;

  private final UserAgentBuilder userAgentBuilder;

  /**
   * Constructor.
   *
   * @param hc4Provider      the {@link HttpClient} provider to be used with this manager.
   * @param userAgentBuilder the {@link UserAgentBuilder} component.
   */
  @Inject
  public HttpClientManagerImpl(final Hc4Provider hc4Provider, final UserAgentBuilder userAgentBuilder) {
    this.hc4Provider = Preconditions.checkNotNull(hc4Provider);
    this.userAgentBuilder = Preconditions.checkNotNull(userAgentBuilder);
  }

  @Override
  public HttpClient create(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    Preconditions.checkNotNull(proxyRepository);
    Preconditions.checkNotNull(ctx);
    final DefaultHttpClient httpClient = (DefaultHttpClient) hc4Provider.createHttpClient(ctx);
    // RRS/Proxy repositories handle retries manually, so kill the retry handler set by Hc4Provider
    // TODO: NEXUS-5368 This is disabled on purpose for now (same in HttpClientManagerTest!)
    // httpClient.setHttpRequestRetryHandler( new StandardHttpRequestRetryHandler( 0, false ) );
    configure(proxyRepository, ctx, httpClient);
    return httpClient;
  }

  @Override
  public void release(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    // nop for now
  }

  // ==

  /**
   * Configures the fresh instance of HttpClient for given proxy repository specific needs. Right now it sets
   * appropriate redirect strategy only.
   */
  protected void configure(final ProxyRepository proxyRepository, final RemoteStorageContext ctx,
                           final DefaultHttpClient httpClient)
  {
    // set UA
    httpClient.getParams().setParameter(HttpProtocolParams.USER_AGENT,
        userAgentBuilder.formatRemoteRepositoryStorageUserAgentString(proxyRepository, ctx));

    // set redirect strategy
    httpClient.setRedirectStrategy(getProxyRepositoryRedirectStrategy(proxyRepository, ctx));
  }

  /**
   * Returns {@link RedirectStrategy} used by proxy repository instances. It as actually preventing HC4 to follow
   * any redirect, as since Nexus 2.8 redirects are handled "manually" in {@link HttpClientRemoteStorage} code.
   *
   * @return the strategy to use with HC4 to follow redirects (basically disabling redirection following of HC4).
   */
  protected RedirectStrategy getProxyRepositoryRedirectStrategy(final ProxyRepository proxyRepository,
                                                                final RemoteStorageContext ctx)
  {
    // Prevent redirection to index pages
    return new DefaultRedirectStrategy()
    {
      @Override
      public boolean isRedirected(final HttpRequest request, final HttpResponse response,
                                  final HttpContext context)
          throws ProtocolException
      {
        return false;
      }
    };
  }
}
