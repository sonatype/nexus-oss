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
package org.sonatype.nexus.httpclient.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;

import org.sonatype.nexus.httpclient.HttpClientPlan;
import org.sonatype.nexus.httpclient.SSLContextSelector;
import org.sonatype.nexus.httpclient.internal.NexusHttpRoutePlanner;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.apache.http.client.config.AuthSchemes.DIGEST;
import static org.apache.http.client.config.AuthSchemes.NTLM;
import static org.sonatype.nexus.httpclient.HttpSchemes.HTTP;
import static org.sonatype.nexus.httpclient.HttpSchemes.HTTPS;

/**
 * Applies {@link HttpClientConfiguration} to {@link HttpClientPlan}.
 *
 * @since 3.0
 */
@SuppressWarnings("PackageAccessibility") // FIXME: httpclient usage is producing lots of OSGI warnings in IDEA
public class ConfigurationCustomizer
    extends ComponentSupport
    implements HttpClientPlan.Customizer
{
  private final HttpClientConfiguration configuration;

  public ConfigurationCustomizer(final HttpClientConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
  }

  @Override
  public void customize(final HttpClientPlan plan) {
    checkNotNull(plan);

    if (configuration.getConnection() != null) {
      apply(configuration.getConnection(), plan);
    }
    if (configuration.getProxy() != null) {
      apply(configuration.getProxy(), plan);
    }
    if (configuration.getAuthentication() != null) {
      apply(configuration.getAuthentication(), plan, null);
    }
  }

  /**
   * Apply connection configuration to plan.
   */
  private void apply(final ConnectionConfiguration connection, final HttpClientPlan plan) {
    if (connection.getTimeout() != null) {
      int timeout = connection.getTimeout().toMillisI();
      plan.getSocket().setSoTimeout(timeout);
      plan.getRequest().setConnectTimeout(timeout);
      plan.getRequest().setSocketTimeout(timeout);
    }

    if (connection.getMaximumRetries() != null) {
      plan.getClient().setRetryHandler(new StandardHttpRequestRetryHandler(connection.getMaximumRetries(), false));
    }

    if (connection.getUserAgentSuffix() != null) {
      checkState(plan.getUserAgent() != null, "Default User-Agent not set");
      plan.getHeaders().put(HttpHeaders.USER_AGENT, plan.getUserAgent() + " " + connection.getUserAgentSuffix());
    }

    if (Boolean.TRUE.equals(connection.getUseTrustStore())) {
      plan.getAttributes().put(SSLContextSelector.USE_TRUST_STORE, Boolean.TRUE);
    }
  }

  /**
   * Apply proxy-server configuration to plan.
   */
  private void apply(final ProxyConfiguration proxy, final HttpClientPlan plan) {
    Map<String, HttpHost> proxies = new HashMap<>(2);

    // HTTP proxy
    ProxyServerConfiguration http = proxy.getHttp();
    if (http != null && http.isEnabled()) {
      HttpHost host = new HttpHost(http.getHost(), http.getPort());
      if (http.getAuthentication() != null) {
        apply(http.getAuthentication(), plan, host);
      }
      proxies.put(HTTP, host);
      proxies.put(HTTPS, host);
    }

    // HTTPS proxy
    ProxyServerConfiguration https = proxy.getHttps();
    if (https != null && https.isEnabled()) {
      HttpHost host = new HttpHost(https.getHost(), https.getPort());
      if (https.getAuthentication() != null) {
        apply(https.getAuthentication(), plan, host);
      }
      proxies.put(HTTPS, host);
    }

    // Non-proxy hosts (nexus-specific regular expression implementation)
    Set<Pattern> patterns = new HashSet<>();
    if (proxy.getNonProxyHosts() != null) {
      for (String regex : proxy.getNonProxyHosts()) {
        try {
          patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
        catch (PatternSyntaxException e) {
          log.warn("Invalid non-proxy host regex: {}", regex, e);
        }
      }
    }
    plan.getClient().setRoutePlanner(new NexusHttpRoutePlanner(proxies, patterns));
  }

  /**
   * Apply authentication configuration to plan.
   */
  private void apply(final AuthenticationConfiguration authentication,
                     final HttpClientPlan plan,
                     @Nullable final HttpHost proxyHost)
  {
    Credentials credentials;
    List<String> authSchemes;

    if (authentication instanceof UsernameAuthenticationConfiguration) {
      UsernameAuthenticationConfiguration auth = (UsernameAuthenticationConfiguration) authentication;
      authSchemes = ImmutableList.of(DIGEST, BASIC);
      credentials = new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword());
    }
    else if (authentication instanceof NtlmAuthenticationConfiguration) {
      NtlmAuthenticationConfiguration auth = (NtlmAuthenticationConfiguration) authentication;
      authSchemes = ImmutableList.of(NTLM, DIGEST, BASIC);
      credentials = new NTCredentials(auth.getUsername(), auth.getPassword(), auth.getHost(), auth.getDomain());
    }
    else {
      throw new IllegalArgumentException("Unsupported authentication configuration: " + authentication);
    }

    if (proxyHost != null) {
      plan.addCredentials(new AuthScope(proxyHost), credentials);
      plan.getRequest().setProxyPreferredAuthSchemes(authSchemes);
    }
    else {
      plan.addCredentials(AuthScope.ANY, credentials);
      plan.getRequest().setTargetPreferredAuthSchemes(authSchemes);
    }
  }
}
