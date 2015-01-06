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
package org.sonatype.nexus.component.source.internal.http;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.http.AuthenticationConfig;
import org.sonatype.nexus.component.source.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.component.source.http.ConnectionConfig;
import org.sonatype.nexus.component.source.http.HttpClientConfig;
import org.sonatype.nexus.component.source.http.HttpClientConfigMarshaller;
import org.sonatype.nexus.component.source.http.HttpProxyConfig;
import org.sonatype.nexus.component.source.http.ProxyConfig;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

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
  public Map<String, Object> toMap(final HttpClientConfig config) {
    checkNotNull(config, "config");
    Map<String, Object> configMap = Maps.newHashMap();
    connectionToMap(configMap, config.getConnectionConfig());
    authenticationToMap(configMap, config.getAuthenticationConfig(), "http");
    proxyToMap(configMap, config.getProxyConfig());
    return configMap;
  }

  private void connectionToMap(final Map<String, Object> configMap,
                               final ConnectionConfig connectionConfig)
  {
    if (connectionConfig != null) {
      putIfNotNull(configMap, "http.connection.timeout", connectionConfig.getTimeout());
      putIfNotNull(configMap, "http.connection.retries", connectionConfig.getRetries());
      putIfNotNull(configMap, "http.connection.urlParameters", connectionConfig.getUrlParameters());
      putIfNotNull(configMap, "http.connection.userAgentCustomisation", connectionConfig.getUserAgentCustomisation());
      putIfNotNull(configMap, "http.connection.useTrustStore", connectionConfig.getUseTrustStore());
    }
  }

  private void authenticationToMap(final Map<String, Object> configMap,
                                   final AuthenticationConfig authenticationConfig,
                                   final String prefix)
  {
    if (authenticationConfig != null) {
      AuthenticationConfigMarshaller marshaller = authenticationConfigMarshallers.get(authenticationConfig.getType());
      if (marshaller != null) {
        Map<String, Object> authMap = checkNotNull(marshaller.toMap(authenticationConfig), "configuration map");
        putIfNotNull(configMap, prefix + ".authentication.type", authenticationConfig.getType());
        for (Entry<String, Object> entry : authMap.entrySet()) {
          putIfNotNull(configMap, prefix + ".authentication." + entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private void proxyToMap(final Map<String, Object> configMap,
                          final ProxyConfig proxyConfig)
  {
    if (proxyConfig != null) {
      httpProxyToMap(configMap, proxyConfig.getHttpProxyConfig(), "http.proxy.http");
      httpProxyToMap(configMap, proxyConfig.getHttpsProxyConfig(), "http.proxy.https");
      if (proxyConfig.getNonProxyHosts() != null) {
        putIfNotNull(configMap, "http.proxy.nonProxyHosts", Joiner.on(",").join(proxyConfig.getNonProxyHosts()));
      }
    }
  }

  private void httpProxyToMap(final Map<String, Object> configMap,
                              final HttpProxyConfig httpProxyConfig,
                              final String prefix)
  {
    if (httpProxyConfig != null) {
      putIfNotNull(configMap, prefix + ".hostname", httpProxyConfig.getHostname());
      putIfNotNull(configMap, prefix + ".port", httpProxyConfig.getPort());
      authenticationToMap(configMap, httpProxyConfig.getAuthenticationConfig(), prefix);
    }
  }

  private void putIfNotNull(final Map<String, Object> config, final String key, final Object value) {
    if (value != null) {
      config.put(key, value);
    }
  }

  @Override
  public HttpClientConfig fromMap(final Map<String, Object> config) {
    HttpClientConfig clientConfig = new HttpClientConfig();
    clientConfig.withConnectionConfig(connectionFromMap(config));
    clientConfig.withAuthenticationConfig(authenticationFromMap(config, "http"));
    clientConfig.withProxyConfig(proxyFromMap(config));
    return clientConfig;
  }

  private ConnectionConfig connectionFromMap(final Map<String, Object> configMap) {
    ConnectionConfig connectionConfig = null;
    if (configMap.containsKey("http.connection.timeout")) {
      connectionConfig = new ConnectionConfig();
      connectionConfig.withTimeout((Integer) configMap.get("http.connection.timeout"));
    }
    if (configMap.containsKey("http.connection.retries")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withRetries((Integer) configMap.get("http.connection.retries"));
    }
    if (configMap.containsKey("http.connection.urlParameters")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUrlParameters((String) configMap.get("http.connection.urlParameters"));
    }
    if (configMap.containsKey("http.connection.userAgentCustomization")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUserAgentCustomisation((String) configMap.get("http.connection.userAgentCustomization"));
    }
    if (configMap.containsKey("http.connection.useTrustStore")) {
      connectionConfig = connectionConfig == null ? new ConnectionConfig() : connectionConfig;
      connectionConfig.withUseTrustStore((Boolean) configMap.get("http.connection.useTrustStore"));
    }
    return connectionConfig;
  }

  private AuthenticationConfig authenticationFromMap(final Map<String, Object> configMap, final String prefix) {
    String type = (String) configMap.get(prefix + ".authentication.type");
    if (type != null) {
      AuthenticationConfigMarshaller marshaller = authenticationConfigMarshallers.get(type);
      Map<String, Object> authMap = Maps.newHashMap();
      for (Entry<String, Object> entry : configMap.entrySet()) {
        if (entry.getKey().startsWith(prefix + ".authentication.")) {
          authMap.put(entry.getKey().substring((prefix + ".authentication.").length()), entry.getValue());
        }
      }
      return marshaller.fromMap(authMap);
    }
    return null;
  }

  private ProxyConfig proxyFromMap(final Map<String, Object> configMap) {
    ProxyConfig proxyConfig = null;
    HttpProxyConfig httpProxyConfig = httpProxyFromMap(configMap, "http.proxy.http");
    if (httpProxyConfig != null) {
      proxyConfig = new ProxyConfig();
      proxyConfig.withHttpProxyConfig(httpProxyConfig);
      proxyConfig.withHttpsProxyConfig(httpProxyFromMap(configMap, "http.proxy.https"));
      if (configMap.containsKey("http.proxy.nonProxyHosts")) {
        proxyConfig.withNonProxyHosts(((String) configMap.get("http.proxy.nonProxyHosts")).split(","));
      }
    }
    return proxyConfig;
  }

  private HttpProxyConfig httpProxyFromMap(final Map<String, Object> configMap,
                                           final String prefix)
  {
    HttpProxyConfig httpProxyConfig = null;
    if (configMap.containsKey(prefix + ".hostname")) {
      httpProxyConfig = new HttpProxyConfig();
      httpProxyConfig.withHostname((String) configMap.get(prefix + ".hostname"));
      httpProxyConfig.withPort((Integer) configMap.get(prefix + ".port"));
      httpProxyConfig.withAuthenticationConfig(authenticationFromMap(configMap, prefix));
    }
    return httpProxyConfig;
  }
}
