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
package org.sonatype.nexus.component.source.api.http;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * HTTP client configuration.
 *
 * @since 3.0
 */
public class HttpClientConfig
{

  private ConnectionConfig connectionConfig;

  private AuthenticationConfig authenticationConfig;

  private ProxyConfig proxyConfig;

  public ConnectionConfig getConnectionConfig() {
    return connectionConfig;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  public ProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public HttpClientConfig withConnectionConfig(final ConnectionConfig connectionConfig) {
    this.connectionConfig = connectionConfig;
    return this;
  }

  public HttpClientConfig withAuthenticationConfig(final AuthenticationConfig authenticationConfig) {
    this.authenticationConfig = authenticationConfig;
    return this;
  }

  public HttpClientConfig withProxyConfig(final ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> config = Maps.newHashMap();
    connectionToMap(config);
    authenticationToMap(config, authenticationConfig, "http");
    proxyToMap(config);
    return config;
  }

  private void connectionToMap(final Map<String, Object> config) {
    if (connectionConfig != null) {
      putIfNotNull(config, "http.connection.timeout", connectionConfig.getTimeout());
      putIfNotNull(config, "http.connection.retries", connectionConfig.getRetries());
      putIfNotNull(config, "http.connection.urlParameters", connectionConfig.getUrlParameters());
      putIfNotNull(config, "http.connection.userAgentCustomization", connectionConfig.getUserAgentCustomisation());
    }
  }

  private void authenticationToMap(final Map<String, Object> config,
                                   final AuthenticationConfig authenticationConfig,
                                   final String prefix)
  {
    if (authenticationConfig != null) {
      if (authenticationConfig instanceof UsernameAuthenticationConfig) {
        UsernameAuthenticationConfig uac = (UsernameAuthenticationConfig) authenticationConfig;
        putIfNotNull(config, prefix + ".authentication.username", uac.getUsername());
        putIfNotNull(config, prefix + ".authentication.password", uac.getPassword());
        if (authenticationConfig instanceof NtlmAuthenticationConfig) {
          NtlmAuthenticationConfig nac = (NtlmAuthenticationConfig) authenticationConfig;
          putIfNotNull(config, prefix + ".authentication.ntlmHost", nac.getNtlmHost());
          putIfNotNull(config, prefix + ".authentication.ntlmDomain", nac.getNtlmDomain());
        }
      }
    }
  }

  private void proxyToMap(final Map<String, Object> config) {
    if (proxyConfig != null) {
      httpProxyToMap(config, proxyConfig.getHttpProxyConfig(), "http.proxy.http");
      httpProxyToMap(config, proxyConfig.getHttpsProxyConfig(), "http.proxy.https");
      putIfNotNull(config, "http.proxy.nonProxyHosts", proxyConfig.getNonProxyHosts());
    }
  }

  private void httpProxyToMap(final Map<String, Object> config,
                              final HttpProxyConfig httpProxyConfig,
                              final String prefix)
  {
    if (httpProxyConfig != null) {
      putIfNotNull(config, prefix + ".hostname", httpProxyConfig.getHostname());
      putIfNotNull(config, prefix + ".port", httpProxyConfig.getPort());
      authenticationToMap(config, httpProxyConfig.getAuthenticationConfig(), prefix);
    }
  }

  private void putIfNotNull(final Map<String, Object> config, final String key, final Object value) {
    if (value != null) {
      config.put(key, value);
    }
  }

  public static HttpClientConfig fromMap(final Map<String, Object> config) {
    HttpClientConfig clientConfig = new HttpClientConfig();
    clientConfig.withConnectionConfig(connectionFromMap(config));
    clientConfig.withAuthenticationConfig(authenticationFromMap(config, "http"));
    clientConfig.withProxyConfig(proxyFromMap(config));
    return clientConfig;
  }

  private static ConnectionConfig connectionFromMap(final Map<String, Object> config) {
    ConnectionConfig connectionConfig = null;
    if (config.containsKey("http.connection.timeout")) {
      connectionConfig = new ConnectionConfig();
      connectionConfig.withTimeout((Integer) config.get("http.connection.timeout"));
    }
    if (config.containsKey("http.connection.retries")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withRetries((Integer) config.get("http.connection.retries"));
    }
    if (config.containsKey("http.connection.urlParameters")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUrlParameters((String) config.get("http.connection.urlParameters"));
    }
    if (config.containsKey("http.connection.userAgentCustomization")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUserAgentCustomisation((String) config.get("http.connection.userAgentCustomization"));
    }
    return connectionConfig;
  }

  private static AuthenticationConfig authenticationFromMap(final Map<String, Object> config, final String prefix) {
    AuthenticationConfig authenticationConfig = null;
    if (config.containsKey(prefix + ".authentication.username")) {
      if (config.containsKey(prefix + ".authentication.ntlmDomain")) {
        authenticationConfig = new NtlmAuthenticationConfig(
            (String) config.get(prefix + ".authentication.username"),
            (String) config.get(prefix + ".authentication.password"),
            (String) config.get(prefix + ".authentication.ntlmHost"),
            (String) config.get(prefix + ".authentication.ntlmDomain")
        );
      }
      else {
        authenticationConfig = new UsernameAuthenticationConfig(
            (String) config.get(prefix + ".authentication.username"),
            (String) config.get(prefix + ".authentication.password")
        );
      }
    }
    return authenticationConfig;
  }

  private static ProxyConfig proxyFromMap(final Map<String, Object> config) {
    ProxyConfig proxyConfig = null;
    HttpProxyConfig httpProxyConfig = httpProxyFromMap(config, "http.proxy.http");
    if (httpProxyConfig != null) {
      proxyConfig = new ProxyConfig();
      proxyConfig.withHttpProxyConfig(httpProxyConfig);
      proxyConfig.withHttpProxyConfig(httpProxyFromMap(config, "http.proxy.https"));
      if (config.containsKey("http.proxy.nonProxyHosts")) {
        proxyConfig.withNonProxyHosts(((String) config.get("http.proxy.nonProxyHosts")).split(","));
      }
    }
    return proxyConfig;
  }

  private static HttpProxyConfig httpProxyFromMap(final Map<String, Object> config,
                                                  final String prefix)
  {
    HttpProxyConfig httpProxyConfig = null;
    if (config.containsKey(prefix + ".hostname")) {
      httpProxyConfig = new HttpProxyConfig();
      httpProxyConfig.withHostname((String) config.get(prefix + ".hostname"));
      httpProxyConfig.withPort((Integer) config.get(prefix + ".port"));
      httpProxyConfig.withAuthenticationConfig(authenticationFromMap(config, prefix));
    }
    return httpProxyConfig;
  }

}
