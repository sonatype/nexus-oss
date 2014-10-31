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
  private String userAgentCustomisation;

  private String urlParameters;

  private Integer timeout;

  private Integer retries;

  private String authUsername;

  private String authPassword;

  private String authNtlmHost;

  private String authNtlmDomain;

  private String httpProxyHost;

  private Integer httpProxyPort;

  private String httpProxyAuthUsername;

  private String httpProxyAuthPassword;

  private String httpProxyAuthNtlmHost;

  private String httpProxyAuthNtlmDomain;

  private String httpsProxyHost;

  private Integer httpsProxyPort;

  private String httpsProxyAuthUsername;

  private String httpsProxyAuthPassword;

  private String httpsProxyAuthNtlmHost;

  private String httpsProxyAuthNtlmDomain;

  private String[] nonProxyHosts;

  public String getUserAgentCustomisation() {
    return userAgentCustomisation;
  }

  public String getUrlParameters() {
    return urlParameters;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public Integer getRetries() {
    return retries;
  }

  public String getAuthUsername() {
    return authUsername;
  }

  public String getAuthPassword() {
    return authPassword;
  }

  public String getAuthNtlmHost() {
    return authNtlmHost;
  }

  public String getAuthNtlmDomain() {
    return authNtlmDomain;
  }

  public String getHttpProxyHost() {
    return httpProxyHost;
  }

  public Integer getHttpProxyPort() {
    return httpProxyPort;
  }

  public String getHttpProxyAuthUsername() {
    return httpProxyAuthUsername;
  }

  public String getHttpProxyAuthPassword() {
    return httpProxyAuthPassword;
  }

  public String getHttpProxyAuthNtlmHost() {
    return httpProxyAuthNtlmHost;
  }

  public String getHttpProxyAuthNtlmDomain() {
    return httpProxyAuthNtlmDomain;
  }

  public String getHttpsProxyHost() {
    return httpsProxyHost;
  }

  public Integer getHttpsProxyPort() {
    return httpsProxyPort;
  }

  public String getHttpsProxyAuthUsername() {
    return httpsProxyAuthUsername;
  }

  public String getHttpsProxyAuthPassword() {
    return httpsProxyAuthPassword;
  }

  public String getHttpsProxyAuthNtlmHost() {
    return httpsProxyAuthNtlmHost;
  }

  public String getHttpsProxyAuthNtlmDomain() {
    return httpsProxyAuthNtlmDomain;
  }

  public String[] getNonProxyHosts() {
    return nonProxyHosts;
  }

  public HttpClientConfig withUserAgentCustomisation(final String userAgentCustomisation) {
    this.userAgentCustomisation = userAgentCustomisation;
    return this;
  }

  public HttpClientConfig withUrlParameters(final String urlParameters) {
    this.urlParameters = urlParameters;
    return this;
  }

  public HttpClientConfig withTimeout(final Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  public HttpClientConfig withRetries(final Integer retries) {
    this.retries = retries;
    return this;
  }

  public HttpClientConfig withNonProxyHosts(final String[] nonProxyHosts) {
    this.nonProxyHosts = nonProxyHosts;
    return this;
  }

  public HttpClientConfig withAuthentication(final String username, final String password) {
    this.authUsername = username;
    this.authPassword = password;
    return this;
  }

  public HttpClientConfig withAuthentication(final String username, final String password,
                                             final String ntlmHost, final String ntlmDomain)
  {
    withAuthentication(username, password);
    this.authNtlmHost = ntlmHost;
    this.authNtlmDomain = ntlmDomain;
    return this;
  }

  public HttpClientConfig withHttpProxy(final String host, final Integer port) {
    this.httpProxyHost = host;
    this.httpProxyPort = port;
    return this;
  }

  public HttpClientConfig withHttpProxy(final String host, final Integer port,
                                        final String username, final String password)
  {
    this.withHttpProxy(host, port);
    this.httpProxyAuthUsername = username;
    this.httpProxyAuthPassword = password;
    return this;
  }

  public HttpClientConfig withHttpProxy(final String host, final Integer port,
                                        final String username, final String password,
                                        final String ntlmHost, final String ntlmDomain)
  {
    this.withHttpProxy(host, port, username, password);
    this.httpProxyAuthNtlmHost = ntlmHost;
    this.httpProxyAuthNtlmDomain = ntlmDomain;
    return this;
  }

  public HttpClientConfig withHttpsProxy(final String host, final Integer port) {
    this.httpsProxyHost = host;
    this.httpsProxyPort = port;
    return this;
  }

  public HttpClientConfig withHttpsProxy(final String host, final Integer port,
                                         final String username, final String password)
  {
    this.withHttpsProxy(host, port);
    this.httpsProxyAuthUsername = username;
    this.httpsProxyAuthPassword = password;
    return this;
  }

  public HttpClientConfig withHttpsProxy(final String host, final Integer port,
                                         final String username, final String password,
                                         final String ntlmHost, final String ntlmDomain)
  {
    this.withHttpsProxy(host, port, username, password);
    this.httpsProxyAuthNtlmHost = ntlmHost;
    this.httpsProxyAuthNtlmDomain = ntlmDomain;
    return this;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> config = Maps.newHashMap();
    putIfNotNull(config, "http.connection.timeout", timeout);
    putIfNotNull(config, "http.connection.retries", retries);
    putIfNotNull(config, "http.connection.urlParameters", urlParameters);
    putIfNotNull(config, "http.connection.userAgentCustomization", userAgentCustomisation);

    putIfNotNull(config, "http.authentication.username", authUsername);
    putIfNotNull(config, "http.authentication.password", authPassword);
    putIfNotNull(config, "http.authentication.ntlmHost", authNtlmHost);
    putIfNotNull(config, "http.authentication.ntlmDomain", authNtlmDomain);

    putIfNotNull(config, "http.proxy.http.host", httpProxyHost);
    putIfNotNull(config, "http.proxy.http.port", httpProxyPort);
    putIfNotNull(config, "http.proxy.http.authentication.username", httpProxyAuthUsername);
    putIfNotNull(config, "http.proxy.http.authentication.password", httpProxyAuthPassword);
    putIfNotNull(config, "http.proxy.http.authentication.ntlmHost", httpProxyAuthNtlmHost);
    putIfNotNull(config, "http.proxy.http.authentication.ntlmDomain", httpProxyAuthNtlmDomain);

    putIfNotNull(config, "http.proxy.https.host", httpsProxyHost);
    putIfNotNull(config, "http.proxy.https.port", httpsProxyPort);
    putIfNotNull(config, "http.proxy.https.authentication.username", httpsProxyAuthUsername);
    putIfNotNull(config, "http.proxy.https.authentication.password", httpsProxyAuthPassword);
    putIfNotNull(config, "http.proxy.https.authentication.ntlmHost", httpsProxyAuthNtlmHost);
    putIfNotNull(config, "http.proxy.https.authentication.ntlmDomain", httpsProxyAuthNtlmDomain);

    putIfNotNull(config, "http.proxy.nonProxyHosts", nonProxyHosts);

    return config;
  }

  private void putIfNotNull(final Map<String, Object> config, final String key, final Object value) {
    if (value != null) {
      config.put(key, value);
    }
  }

  public static HttpClientConfig fromMap(final Map<String, Object> config) {
    HttpClientConfig clientConfig = new HttpClientConfig();
    applyConnection(config, clientConfig);
    applyAuthentication(config, clientConfig);
    applyProxy(config, clientConfig);
    return clientConfig;
  }

  private static void applyConnection(final Map<String, Object> config, final HttpClientConfig clientConfig) {
    if (config.containsKey("http.connection.timeout")) {
      clientConfig.withTimeout((Integer) config.get("http.connection.timeout"));
    }
    if (config.containsKey("http.connection.retries")) {
      clientConfig.withRetries((Integer) config.get("http.connection.retries"));
    }
    if (config.containsKey("http.connection.urlParameters")) {
      clientConfig.withUrlParameters((String) config.get("http.connection.urlParameters"));
    }
    if (config.containsKey("http.connection.userAgentCustomization")) {
      clientConfig.withUserAgentCustomisation((String) config.get("http.connection.userAgentCustomization"));
    }
  }

  private static void applyAuthentication(final Map<String, Object> config, final HttpClientConfig clientConfig) {
    if (config.containsKey("http.authentication.username")) {
      if (config.containsKey("http.authentication.ntlmDomain")) {
        clientConfig.withAuthentication(
            (String) config.get("http.authentication.username"),
            (String) config.get("http.authentication.password"),
            (String) config.get("http.authentication.ntlmHost"),
            (String) config.get("http.authentication.ntlmDomain")
        );
      }
      else {
        clientConfig.withAuthentication(
            (String) config.get("http.authentication.username"),
            (String) config.get("http.authentication.password")
        );
      }
    }
  }

  private static void applyProxy(final Map<String, Object> config, final HttpClientConfig clientConfig) {
    applyHttpProxy(config, clientConfig);
    applyHttpsProxy(config, clientConfig);
    if (config.containsKey("http.proxy.nonProxyHosts")) {
      clientConfig.withNonProxyHosts(((String) config.get("http.connection.userAgentCustomization")).split(","));
    }
  }

  private static void applyHttpProxy(final Map<String, Object> config, final HttpClientConfig clientConfig) {
    if (config.containsKey("http.proxy.http.host")) {
      if (config.containsKey("http.proxy.http.authentication.username")) {
        if (config.containsKey("http.proxy.http.authentication.ntlmDomain")) {
          clientConfig.withHttpProxy(
              (String) config.get("http.proxy.http.host"),
              (Integer) config.get("http.proxy.http.port"),
              (String) config.get("http.proxy.http.authentication.username"),
              (String) config.get("http.proxy.http.authentication.password"),
              (String) config.get("http.proxy.http.authentication.ntlmHost"),
              (String) config.get("http.proxy.http.authentication.ntlmDomain")
          );
        }
        else {
          clientConfig.withHttpProxy(
              (String) config.get("http.proxy.http.host"),
              (Integer) config.get("http.proxy.http.port"),
              (String) config.get("http.proxy.http.authentication.username"),
              (String) config.get("http.proxy.http.authentication.password")
          );
        }
      }
      else {
        clientConfig.withHttpProxy(
            (String) config.get("http.proxy.http.host"),
            (Integer) config.get("http.proxy.http.port")
        );
      }
    }
  }

  private static void applyHttpsProxy(final Map<String, Object> config, final HttpClientConfig clientConfig) {
    if (config.containsKey("http.proxy.https.host")) {
      if (config.containsKey("http.proxy.https.authentication.username")) {
        if (config.containsKey("http.proxy.https.authentication.ntlmDomain")) {
          clientConfig.withHttpProxy(
              (String) config.get("http.proxy.https.host"),
              (Integer) config.get("http.proxy.https.port"),
              (String) config.get("http.proxy.https.authentication.username"),
              (String) config.get("http.proxy.https.authentication.password"),
              (String) config.get("http.proxy.https.authentication.ntlmHost"),
              (String) config.get("http.proxy.https.authentication.ntlmDomain")
          );
        }
        else {
          clientConfig.withHttpProxy(
              (String) config.get("http.proxy.https.host"),
              (Integer) config.get("http.proxy.https.port"),
              (String) config.get("http.proxy.https.authentication.username"),
              (String) config.get("http.proxy.https.authentication.password")
          );
        }
      }
      else {
        clientConfig.withHttpProxy(
            (String) config.get("http.proxy.https.host"),
            (Integer) config.get("http.proxy.https.port")
        );
      }
    }
  }

}
