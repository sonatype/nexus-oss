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
package org.sonatype.nexus.component.source.internal.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.http.AuthenticationConfig;
import org.sonatype.nexus.component.source.api.http.HttpClientConfig;
import org.sonatype.nexus.component.source.api.http.HttpClientFactory;
import org.sonatype.nexus.component.source.api.http.HttpProxyConfig;
import org.sonatype.nexus.component.source.api.http.NtlmAuthenticationConfig;
import org.sonatype.nexus.component.source.api.http.UsernameAuthenticationConfig;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.httpclient.HttpClientFactory.Customizer;
import org.sonatype.nexus.httpclient.NexusHttpRoutePlanner;
import org.sonatype.nexus.httpclient.NexusRedirectStrategy;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link HttpClientFactory} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class HttpClientFactoryImpl
    extends ComponentSupport
    implements HttpClientFactory
{

  private org.sonatype.nexus.httpclient.HttpClientFactory httpClientFactory;

  @Inject
  public HttpClientFactoryImpl(final org.sonatype.nexus.httpclient.HttpClientFactory httpClientFactory) {
    this.httpClientFactory = checkNotNull(httpClientFactory);
  }

  @Override
  public HttpClient create(final HttpClientConfig config) {
    return httpClientFactory.create(new Customizer()
    {
      @Override
      public void customize(final Builder builder) {
        applyConfiguration(builder, config);
        applyRedirectStrategy(builder);
      }
    });
  }

  private void applyConfiguration(final Builder builder,
                                  final HttpClientConfig config)
  {
    // connection/socket timeouts
    int timeout = 1000;
    if (config.getConnectionConfig() != null && config.getConnectionConfig().getTimeout() != null) {
      timeout = config.getConnectionConfig().getTimeout();
    }
    builder.getSocketConfigBuilder().setSoTimeout(timeout);
    builder.getRequestConfigBuilder().setConnectTimeout(timeout);
    builder.getRequestConfigBuilder().setSocketTimeout(timeout);

    // obey the given retries count and apply it to client.
    int retries = 0;
    if (config.getConnectionConfig() != null && config.getConnectionConfig().getRetries() != null) {
      retries = config.getConnectionConfig().getRetries();
    }
    builder.getHttpClientBuilder().setRetryHandler(new StandardHttpRequestRetryHandler(retries, false));

    applyAuthenticationConfig(builder, config.getAuthenticationConfig(), null);
    applyProxyConfig(builder, config);

    // Apply optional context-specific user-agent suffix
    if (config.getConnectionConfig() != null) {
      String userAgentSuffix = config.getConnectionConfig().getUserAgentCustomisation();
      if (!StringUtils.isEmpty(userAgentSuffix)) {
        final String customizedUserAgent = builder.getUserAgent() + " " + userAgentSuffix;
        builder.setUserAgent(customizedUserAgent);
        builder.getHttpClientBuilder().setRequestExecutor(new HttpRequestExecutor()
        {
          @Override
          public void preProcess(final HttpRequest request, final HttpProcessor processor, final HttpContext ctx)
              throws HttpException, IOException
          {
            // NEXUS-7575: In case of HTTP Proxy tunnel, add generic UA while performing CONNECT
            if (!request.containsHeader(HTTP.USER_AGENT)) {
              request.addHeader(new BasicHeader(HTTP.USER_AGENT, customizedUserAgent));
            }
            super.preProcess(request, processor, ctx);
          }
        });
      }
    }
  }

  @VisibleForTesting
  public void applyAuthenticationConfig(final Builder builder,
                                        final AuthenticationConfig authenticationConfig,
                                        final HttpHost proxyHost)
  {
    if (authenticationConfig != null) {
      String authScope = "target";
      if (proxyHost != null) {
        authScope = proxyHost.toHostString() + " proxy";
      }

      final List<String> authorisationPreference = Lists.newArrayListWithExpectedSize(3);
      authorisationPreference.add(AuthSchemes.DIGEST);
      authorisationPreference.add(AuthSchemes.BASIC);
      Credentials credentials;
      if (authenticationConfig instanceof NtlmAuthenticationConfig) {
        NtlmAuthenticationConfig nac = (NtlmAuthenticationConfig) authenticationConfig;
        // Using NTLM auth, adding it as first in policies
        authorisationPreference.add(0, AuthSchemes.NTLM);
        log.debug("{} authentication setup for NTLM domain '{}'", authScope, nac.getNtlmDomain());
        credentials = new NTCredentials(nac.getUsername(), nac.getPassword(), nac.getNtlmHost(), nac.getNtlmDomain());
      }
      else if (authenticationConfig instanceof UsernameAuthenticationConfig) {
        UsernameAuthenticationConfig uac = (UsernameAuthenticationConfig) authenticationConfig;
        log.debug("{} authentication setup for remote storage with username '{}'", authScope, uac.getUsername());
        credentials = new UsernamePasswordCredentials(uac.getUsername(), uac.getPassword());
      }
      else {
        throw new IllegalArgumentException("Unsupported authentication config: " + authenticationConfig.getClass());
      }

      if (proxyHost != null) {
        builder.setCredentials(new AuthScope(proxyHost), credentials);
        builder.getRequestConfigBuilder().setProxyPreferredAuthSchemes(authorisationPreference);
      }
      else {
        builder.setCredentials(AuthScope.ANY, credentials);
        builder.getRequestConfigBuilder().setTargetPreferredAuthSchemes(authorisationPreference);
      }
    }
  }

  @VisibleForTesting
  public void applyProxyConfig(final Builder builder, final HttpClientConfig config) {
    if (config.getProxyConfig() != null && config.getProxyConfig().getHttpProxyConfig() != null) {
      Map<String, HttpHost> proxies = Maps.newHashMap();

      HttpProxyConfig httpProxyConfig = config.getProxyConfig().getHttpProxyConfig();
      HttpHost httpProxy = new HttpHost(httpProxyConfig.getHostname(), httpProxyConfig.getPort());
      applyAuthenticationConfig(builder, httpProxyConfig.getAuthenticationConfig(), httpProxy);

      log.debug("http proxy setup with host '{}'", httpProxyConfig.getHostname());
      proxies.put("http", httpProxy);
      proxies.put("https", httpProxy);

      if (config.getProxyConfig().getHttpsProxyConfig() != null) {
        HttpProxyConfig httpsProxyConfig = config.getProxyConfig().getHttpsProxyConfig();
        HttpHost httpsProxy = new HttpHost(httpsProxyConfig.getHostname(), httpsProxyConfig.getPort());
        applyAuthenticationConfig(builder, httpsProxyConfig.getAuthenticationConfig(), httpsProxy);
        log.debug("https proxy setup with host '{}'", httpsProxy.getHostName());
        proxies.put("https", httpsProxy);
      }

      final Set<Pattern> nonProxyHostPatterns = Sets.newHashSet();
      if (config.getProxyConfig().getNonProxyHosts() != null) {
        for (String nonProxyHostRegex : config.getProxyConfig().getNonProxyHosts()) {
          try {
            nonProxyHostPatterns.add(Pattern.compile(nonProxyHostRegex, Pattern.CASE_INSENSITIVE));
          }
          catch (PatternSyntaxException e) {
            log.warn("Invalid non proxy host regex: {}", nonProxyHostRegex, e);
          }
        }
      }

      builder.getHttpClientBuilder().setRoutePlanner(
          new NexusHttpRoutePlanner(proxies, nonProxyHostPatterns, DefaultSchemePortResolver.INSTANCE)
      );
    }
  }

  private void applyRedirectStrategy(final Builder builder) {
    builder.getHttpClientBuilder().setRedirectStrategy(new NexusRedirectStrategy());
  }

}
