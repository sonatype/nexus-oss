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
package org.sonatype.nexus.repository.http;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.AuthenticationConfig;
import org.sonatype.nexus.repository.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.repository.http.ConnectionConfig;
import org.sonatype.nexus.repository.http.HttpClientConfig;
import org.sonatype.nexus.repository.http.HttpClientConfigMarshaller;
import org.sonatype.nexus.repository.http.HttpProxyConfig;
import org.sonatype.nexus.repository.http.ProxyConfig;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link HttpClientConfigMarshaller} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class HttpClientConfigMarshallerImpl
    extends ComponentSupport
    implements HttpClientConfigMarshaller
{
  private final Map<String, AuthenticationConfigMarshaller> authenticationConfigMarshallers;

  @Inject
  public HttpClientConfigMarshallerImpl(
      final Map<String, AuthenticationConfigMarshaller> authenticationConfigMarshallers)
  {
    this.authenticationConfigMarshallers = checkNotNull(authenticationConfigMarshallers);
  }

  @Override
  public void toMap(final HttpClientConfig config, final NestedAttributesMap map) {
    checkNotNull(config);
    checkNotNull(map);
    final NestedAttributesMap httpConfig = map.child("http");
    connectionToMap(httpConfig.child("connection"), config.getConnectionConfig());
    authenticationToMap(httpConfig.child("authentication"), config.getAuthenticationConfig());
    proxyToMap(httpConfig.child("proxy"), config.getProxyConfig());
  }

  private void connectionToMap(final NestedAttributesMap configMap,
                               final ConnectionConfig connectionConfig)
  {
    if (connectionConfig != null) {
      putIfNotNull(configMap, "timeout", connectionConfig.getTimeout());
      putIfNotNull(configMap, "retries", connectionConfig.getRetries());
      putIfNotNull(configMap, "urlParameters", connectionConfig.getUrlParameters());
      putIfNotNull(configMap, "userAgentCustomisation", connectionConfig.getUserAgentCustomisation());
      putIfNotNull(configMap, "useTrustStore", connectionConfig.getUseTrustStore());
    }
  }

  private void authenticationToMap(final NestedAttributesMap configMap,
                                   final AuthenticationConfig authenticationConfig)
  {
    if (authenticationConfig != null) {
      AuthenticationConfigMarshaller marshaller = authenticationConfigMarshallers.get(authenticationConfig.getType());
      if (marshaller != null) {
        putIfNotNull(configMap, "type", authenticationConfig.getType());
        marshaller.toMap(authenticationConfig, configMap);
      }
    }
  }

  private void proxyToMap(final NestedAttributesMap configMap,
                          final ProxyConfig proxyConfig)
  {
    if (proxyConfig != null) {
      httpProxyToMap(configMap.child("http"), proxyConfig.getHttpProxyConfig());
      httpProxyToMap(configMap.child("https"), proxyConfig.getHttpsProxyConfig());
      if (proxyConfig.getNonProxyHosts() != null) {
        putIfNotNull(configMap, "nonProxyHosts", Joiner.on(",").join(proxyConfig.getNonProxyHosts()));
      }
    }
  }

  private void httpProxyToMap(final NestedAttributesMap configMap,
                              final HttpProxyConfig httpProxyConfig)
  {
    if (httpProxyConfig != null) {
      putIfNotNull(configMap, "hostname", httpProxyConfig.getHostname());
      putIfNotNull(configMap, "port", httpProxyConfig.getPort());
      authenticationToMap(configMap.child("authentication"), httpProxyConfig.getAuthenticationConfig());
    }
  }

  private void putIfNotNull(final NestedAttributesMap config, final String key, final Object value) {
    if (value != null) {
      config.set(key, value);
    }
  }

  @Override
  public HttpClientConfig fromMap(final NestedAttributesMap config) {
    HttpClientConfig clientConfig = new HttpClientConfig();
    final NestedAttributesMap httpConfig = config.child("http");
    clientConfig.withConnectionConfig(connectionFromMap(httpConfig.child("connection")));
    clientConfig.withAuthenticationConfig(authenticationFromMap(httpConfig.child("authentication")));
    clientConfig.withProxyConfig(proxyFromMap(httpConfig.child("proxy")));
    return clientConfig;
  }

  private ConnectionConfig connectionFromMap(final NestedAttributesMap configMap) {
    ConnectionConfig connectionConfig = null;

    if (configMap.contains("timeout")) {
      connectionConfig = new ConnectionConfig();
      connectionConfig.withTimeout((Integer) configMap.get("timeout"));
    }
    if (configMap.contains("retries")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withRetries((Integer) configMap.get("retries"));
    }
    if (configMap.contains("urlParameters")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUrlParameters((String) configMap.get("urlParameters"));
    }
    if (configMap.contains("userAgentCustomization")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUserAgentCustomisation((String) configMap.get("userAgentCustomization"));
    }
    if (configMap.contains("useTrustStore")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUseTrustStore((Boolean) configMap.get("useTrustStore"));
    }
    return connectionConfig;
  }

  private ProxyConfig proxyFromMap(final NestedAttributesMap configMap) {
    ProxyConfig proxyConfig = null;
    HttpProxyConfig httpProxyConfig = httpProxyFromMap(configMap.child("http"));
    if (httpProxyConfig != null) {
      proxyConfig = new ProxyConfig();
      proxyConfig.withHttpProxyConfig(httpProxyConfig);
      proxyConfig.withHttpsProxyConfig(httpProxyFromMap(configMap.child("https")));
      if (configMap.contains("nonProxyHosts")) {
        proxyConfig.withNonProxyHosts((configMap.require("nonProxyHosts", String.class)).split(","));
      }
    }
    return proxyConfig;
  }

  private HttpProxyConfig httpProxyFromMap(final NestedAttributesMap configMap)
  {
    HttpProxyConfig httpProxyConfig = null;
    if (configMap.contains("hostname")) {
      httpProxyConfig = new HttpProxyConfig();
      httpProxyConfig.withHostname((String) configMap.get("hostname"));
      httpProxyConfig.withPort((Integer) configMap.get("port"));
      httpProxyConfig.withAuthenticationConfig(authenticationFromMap(configMap.child("authentication")));
    }
    return httpProxyConfig;
  }

  private AuthenticationConfig authenticationFromMap(final NestedAttributesMap configMap) {

    if (configMap.contains("type")) {
      final AuthenticationConfigMarshaller marshaller = authenticationConfigMarshallers.get(configMap.get("type"));
      return marshaller.fromMap(configMap);
    }
    return null;
  }
}
