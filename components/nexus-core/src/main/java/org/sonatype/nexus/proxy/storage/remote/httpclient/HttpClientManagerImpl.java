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
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.property.SystemPropertiesHelper;
import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.httpclient.NexusRedirectStrategy;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.client.HttpClient;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.impl.client.BasicCookieStore;

import static com.google.common.base.Preconditions.checkNotNull;

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
  private static final String NX_REMOTE_ENABLE_CIRCULAR_REDIRECTS_KEY = "nexus.remoteStorage.enableCircularRedirectsForHosts";

  private static final String NX_REMOTE_USE_COOKIES_KEY = "nexus.remoteStorage.useCookiesForHosts";

  private final HttpClientFactory httpClientFactory;

  private final Set<String> enableCircularRedirectsForHosts;

  private final Set<String> useCookiesForHosts;

  @Inject
  public HttpClientManagerImpl(final HttpClientFactory httpClientFactory) {
    this.httpClientFactory = checkNotNull(httpClientFactory);
    this.enableCircularRedirectsForHosts = parseAndNormalizeCsvProperty(NX_REMOTE_ENABLE_CIRCULAR_REDIRECTS_KEY);
    this.useCookiesForHosts = parseAndNormalizeCsvProperty(NX_REMOTE_USE_COOKIES_KEY);
  }

  @Override
  public HttpClient create(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    checkNotNull(proxyRepository);
    checkNotNull(ctx);
    final Builder builder = httpClientFactory.prepare(new RemoteStorageContextCustomizer(ctx));
    configure(proxyRepository, builder);
    return builder.build();
  }

  @Override
  public void release(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    // nop for now
  }

  // ==

  /**
   * Normalizes proxy repository's hostname extracted from it's {@link ProxyRepository#getRemoteUrl()} method. Never
   * returns {@code null}.
   *
   * @see #normalizeHostname(String)
   */
  private String normalizeHostname(final ProxyRepository proxyRepository) {
    try {
      final URI uri = new URI(proxyRepository.getRemoteUrl());
      return normalizeHostname(uri.getHost());
    }
    catch (URISyntaxException e) {
      // ignore
    }
    return "";
  }

  /**
   * Normalizes passed in host name string by lower casing it. Never returns {@code null} even if input was {@code
   * null}.
   */
  private String normalizeHostname(final String hostName) {
    if (hostName == null) {
      return "";
    } else {
      return hostName.toLowerCase(Locale.US).trim();
    }
  }

  /**
   * Parses and normalizes (by lower-casing) CSV of host names under given property key. Never returns {@code null}.
   * Never returns a set that contains {@code null} or empty strings.
   */
  private Set<String> parseAndNormalizeCsvProperty(final String systemPropertyKey) {
    return Sets.newHashSet(
        Iterables.transform(
            Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(SystemPropertiesHelper.getString(systemPropertyKey, "")),
            new Function<String, String>()
            {
              @Override
              public String apply(final String input) {
                return normalizeHostname(input);
              }
            }
        )
    );
  }

  /**
   * Configures the fresh instance of HttpClient for given proxy repository specific needs. Right now it sets
   * appropriate redirect strategy only.
   */
  private void configure(final ProxyRepository proxyRepository, final Builder builder) {
    // set proxy redirect strategy
    builder.getHttpClientBuilder().setRedirectStrategy(new NexusRedirectStrategy());

    // MEXUS-7915: Allow use of circular redirects, if set
    final String proxyHostName = normalizeHostname(proxyRepository);
    if (enableCircularRedirectsForHosts.contains(proxyHostName)) {
      log.info("Allowing circular redirects in proxy {}", proxyRepository);
      builder.getRequestConfigBuilder().setCircularRedirectsAllowed(true); // allow circular redirects
      builder.getRequestConfigBuilder().setMaxRedirects(10); // lessen max redirects from default 50
    }
    // MEXUS-7915: Allow use of cookies, if set
    if (useCookiesForHosts.contains(proxyHostName)) {
      log.info("Allowing cookie use in proxy {}", proxyRepository);
      builder.getHttpClientBuilder().setDefaultCookieStore(new BasicCookieStore()); // in memory only
      builder.getRequestConfigBuilder().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY); // emulate browsers
    }
  }
}
