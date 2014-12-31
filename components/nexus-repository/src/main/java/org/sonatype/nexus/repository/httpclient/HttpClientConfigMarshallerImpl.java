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
package org.sonatype.nexus.repository.httpclient;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
  private final Map<String, AuthenticationConfigMarshaller> marshallers;

  @Inject
  public HttpClientConfigMarshallerImpl(final Map<String, AuthenticationConfigMarshaller> marshallers) {
    this.marshallers = checkNotNull(marshallers);
  }

  @Override
  public void toMap(final HttpClientConfig config, final NestedAttributesMap attributes) {
    checkNotNull(config);
    checkNotNull(attributes);
    connectionToMap(attributes.child("connection"), config.getConnectionConfig());
    authenticationToMap(attributes.child("authentication"), config.getAuthenticationConfig());
    proxyToMap(attributes.child("proxy"), config.getProxyConfig());
  }

  private void connectionToMap(final NestedAttributesMap attributes, final ConnectionConfig connection) {
    if (connection != null) {
      putIfNotNull(attributes, "timeout", connection.getTimeout());
      putIfNotNull(attributes, "retries", connection.getRetries());
      putIfNotNull(attributes, "urlParameters", connection.getUrlParameters());
      putIfNotNull(attributes, "userAgentCustomisation", connection.getUserAgentCustomisation());
      putIfNotNull(attributes, "useTrustStore", connection.getUseTrustStore());
    }
  }

  private void authenticationToMap(final NestedAttributesMap attributes, final AuthenticationConfig authentication) {
    if (authentication != null) {
      AuthenticationConfigMarshaller marshaller = marshallers.get(authentication.getType());
      if (marshaller != null) {
        attributes.set("type", authentication.getType());
        marshaller.toMap(authentication, attributes);
      }
    }
  }

  private void proxyToMap(final NestedAttributesMap attributes, final ProxyConfig proxy) {
    if (proxy != null) {
      httpProxyToMap(attributes.child("http"), proxy.getHttpProxyConfig());
      httpProxyToMap(attributes.child("https"), proxy.getHttpsProxyConfig());
      if (proxy.getNonProxyHosts() != null) {
        putIfNotNull(attributes, "nonProxyHosts", Joiner.on(",").join(proxy.getNonProxyHosts()));
      }
    }
  }

  private void httpProxyToMap(final NestedAttributesMap attributes, final HttpProxyConfig httpProxy) {
    if (httpProxy != null) {
      putIfNotNull(attributes, "hostname", httpProxy.getHostname());
      putIfNotNull(attributes, "port", httpProxy.getPort());
      authenticationToMap(attributes.child("authentication"), httpProxy.getAuthenticationConfig());
    }
  }

  private void putIfNotNull(final NestedAttributesMap attributes, final String key, final Object value) {
    if (value != null) {
      attributes.set(key, value);
    }
  }

  @Override
  public HttpClientConfig fromMap(final NestedAttributesMap attributes) {
    HttpClientConfig httpClient = new HttpClientConfig();
    httpClient.setConnectionConfig(connectionFromMap(attributes.child("connection")));
    httpClient.setAuthenticationConfig(authenticationFromMap(attributes.child("authentication")));
    httpClient.setProxyConfig(proxyFromMap(attributes.child("proxy")));
    return httpClient;
  }

  private ConnectionConfig connectionFromMap(final NestedAttributesMap attributes) {
    ConnectionConfig connection = null;

    if (attributes.contains("timeout")) {
      connection = new ConnectionConfig();
      connection.setTimeout(attributes.get("timeout", Integer.class));
    }
    if (attributes.contains("retries")) {
      connection = connection == null ? new ConnectionConfig() : connection;
      connection.setRetries(attributes.get("retries", Integer.class));
    }
    if (attributes.contains("urlParameters")) {
      connection = connection == null ? new ConnectionConfig() : connection;
      connection.setUrlParameters(attributes.get("urlParameters", String.class));
    }
    if (attributes.contains("userAgentCustomization")) {
      connection = connection == null ? new ConnectionConfig() : connection;
      connection.setUserAgentCustomisation(attributes.get("userAgentCustomization", String.class));
    }
    if (attributes.contains("useTrustStore")) {
      connection = connection == null ? new ConnectionConfig() : connection;
      connection.setUseTrustStore(attributes.get("useTrustStore", Boolean.class));
    }
    return connection;
  }

  private ProxyConfig proxyFromMap(final NestedAttributesMap attributes) {
    ProxyConfig proxy = null;
    HttpProxyConfig httpProxy = httpProxyFromMap(attributes.child("http"));
    if (httpProxy != null) {
      proxy = new ProxyConfig();
      proxy.withHttpProxyConfig(httpProxy);
      proxy.withHttpsProxyConfig(httpProxyFromMap(attributes.child("https")));
      if (attributes.contains("nonProxyHosts")) {
        proxy.withNonProxyHosts((attributes.require("nonProxyHosts", String.class)).split(","));
      }
    }
    return proxy;
  }

  private HttpProxyConfig httpProxyFromMap(final NestedAttributesMap attributes) {
    HttpProxyConfig httpProxy = null;
    if (attributes.contains("hostname")) {
      httpProxy = new HttpProxyConfig();
      httpProxy.setHostname(attributes.get("hostname", String.class));
      httpProxy.setPort(attributes.get("port", Integer.class));
      httpProxy.setAuthenticationConfig(authenticationFromMap(attributes.child("authentication")));
    }
    return httpProxy;
  }

  private AuthenticationConfig authenticationFromMap(final NestedAttributesMap attributes) {
    String type = attributes.get("type", String.class);
    if (type != null) {
      AuthenticationConfigMarshaller marshaller = marshallers.get(type);
      checkState(marshaller != null);
      return marshaller.fromMap(attributes);
    }
    return null;
  }
}
