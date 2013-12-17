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

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteItemNotFoundException;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

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
    extends AbstractLoggingComponent
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
   * Returns {@link RedirectStrategy} used by proxy repository instances. For now, it is "do not follow redirect to
   * index pages (collections), but accept and follow any other redirects" strategy. If index page redirect is detected
   * (by naive checking the URL for trailing slash), redirection mechanism of HC4 is stopped, and hence, the response
   * will return with redirect response code (301, 302 or 307). These responses are handled within {@link
   * HttpClientRemoteStorage} and is handled by throwing a {@link RemoteItemNotFoundException}. Main goal of this
   * {@link RedirectStrategy} is to save the subsequent (the one following the redirect) request once we learn
   * it would lead us to index page, as we don't need index pages (hence, we do not fetch it only to throw it away).
   * <p/>
   * Usual problems are misconfiguration, where a repository published over HTTPS is configured with HTTP (ie.
   * admin mistyped the URL). Seemingly all work, but that is a source of performance issue, as every outgoing
   * Nexus request will "bounce", as usually HTTP port will redirect Nexus to HTTPS port, and then the artifact
   * will be fetched. Remedy for these scenarios is to edit the proxy repository configuration and update the
   * URL to proper protocol.
   * <p/>
   * Still, this code is very naive way to detect index page redirects, and is used only in Nexuses released prior
   * 2.8 release.
   * <p/>
   * This code <strong>assumes</strong> that remote repository is set up by best practices and common conventions,
   * hence, index page redirect means that target URL ends with slash. For more about this topic, read the
   * "To slash or not to slash" Google blog entry.
   *
   * @return the strategy to use with HC4 to follow redirects.
   * @see <a href="http://googlewebmastercentral.blogspot.hu/2010/04/to-slash-or-not-to-slash.html">To slash or not to
   * slash</a>
   */
  protected RedirectStrategy getProxyRepositoryRedirectStrategy(final ProxyRepository proxyRepository,
                                                                final RemoteStorageContext ctx)
  {
    // Prevent redirection to index pages
    final RedirectStrategy doNotRedirectToIndexPagesStrategy = new DefaultRedirectStrategy()
    {
      @Override
      public boolean isRedirected(final HttpRequest request, final HttpResponse response,
                                  final HttpContext context)
          throws ProtocolException
      {
        if (super.isRedirected(request, response, context)) {
          if (response.getFirstHeader("location") != null) {
            final String targetUriHeader = response.getFirstHeader("location").getValue();
            // is this an index page redirect?
            try {
              // create URI to access path, as location might have query parameters
              final URI targetUri = new URI(targetUriHeader);
              if (targetUri.getPath().endsWith("/")) {
                return false; // this is index page, break the redirect following and make HC4 return the redirecting response
              }
            }
            catch (URISyntaxException e) {
              // fallback to "naive" string checking
              if (targetUriHeader.endsWith("/")) {
                return false; // this is index page, break the redirect following and make HC4 return the redirecting response
              }
            }
          }
          return true;
        }
        return false;
      }
    };
    return doNotRedirectToIndexPagesStrategy;
  }
}
