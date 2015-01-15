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
  private final Map<String, AuthenticationConfig.Marshaller> marshallers;

  @Inject
  public HttpClientConfigMarshallerImpl(final Map<String, AuthenticationConfig.Marshaller> marshallers) {
    this.marshallers = checkNotNull(marshallers);
  }

  private AuthenticationConfig.Marshaller marshaller(final String type) {
    AuthenticationConfig.Marshaller marshaller = marshallers.get(type);
    checkState(marshaller != null, "Missing marshaller for type: %s", type);
    return marshaller;
  }

  //
  // Marshall
  //

  @Override
  public void marshall(final HttpClientConfig config, final NestedAttributesMap attributes) {
    checkNotNull(config);
    checkNotNull(attributes);

    if (config.getConnectionConfig() != null) {
      write(attributes.child("connection"), config.getConnectionConfig());
    }
    if (config.getAuthenticationConfig() != null) {
      write(attributes.child("authentication"), config.getAuthenticationConfig());
    }
    if (config.getProxyConfig() != null) {
      write(attributes.child("proxy"), config.getProxyConfig());
    }
  }

  private void write(final NestedAttributesMap attributes, final ConnectionConfig config) {
    attributes.set("timeout", config.getTimeout());
    attributes.set("retries", config.getRetries());
    attributes.set("urlParameters", config.getUrlParameters());
    attributes.set("userAgentCustomisation", config.getUserAgentCustomisation());
    attributes.set("useTrustStore", config.getUseTrustStore());
  }

  private void write(final NestedAttributesMap attributes, final AuthenticationConfig config) {
    AuthenticationConfig.Marshaller marshaller = marshaller(config.getType());
    attributes.set("type", config.getType());
    marshaller.marshall(config, attributes);
  }

  private void write(final NestedAttributesMap attributes, final ProxyConfig config) {
    if (config.getHttpProxyConfig() != null) {
      write(attributes.child("http"), config.getHttpProxyConfig());
    }
    if (config.getHttpsProxyConfig() != null) {
      write(attributes.child("https"), config.getHttpsProxyConfig());
    }
    if (config.getNonProxyHosts() != null) {
      attributes.set("nonProxyHosts", Joiner.on(",").join(config.getNonProxyHosts()));
    }
  }

  private void write(final NestedAttributesMap attributes, final HttpProxyConfig config) {
    attributes.set("hostname", config.getHostname());
    attributes.set("port", config.getPort());
    if (config.getAuthenticationConfig() != null) {
      write(attributes.child("authentication"), config.getAuthenticationConfig());
    }
  }

  //
  // Unmarshall
  //

  @Override
  public HttpClientConfig unmarshall(final NestedAttributesMap attributes) {
    checkNotNull(attributes);

    HttpClientConfig config = new HttpClientConfig();

    if (attributes.contains("connection")) {
      config.setConnectionConfig(readConnection(attributes.child("connection")));
    }
    if (attributes.contains("authentication")) {
      config.setAuthenticationConfig(readAuthentication(attributes.child("authentication")));
    }
    if (attributes.contains("proxy")) {
      config.setProxyConfig(readProxy(attributes.child("proxy")));
    }
    return config;
  }

  private ConnectionConfig readConnection(final NestedAttributesMap attributes) {
    ConnectionConfig config = new ConnectionConfig();

    config.setTimeout(attributes.get("timeout", Integer.class));
    config.setRetries(attributes.get("retries", Integer.class));
    config.setUrlParameters(attributes.get("urlParameters", String.class));
    config.setUserAgentCustomisation(attributes.get("userAgentCustomization", String.class));
    config.setUseTrustStore(attributes.get("useTrustStore", Boolean.class));

    return config;
  }

  private ProxyConfig readProxy(final NestedAttributesMap attributes) {
    ProxyConfig config = new ProxyConfig();

    if (attributes.contains("http")) {
      config.setHttpProxyConfig(readHttpProxy(attributes.child("http")));
    }
    if (attributes.contains("https")) {
      config.setHttpsProxyConfig(readHttpProxy(attributes.child("https")));
    }
    if (attributes.contains("nonProxyHosts")) {
      config.setNonProxyHosts((attributes.require("nonProxyHosts", String.class)).split(","));
    }

    return config;
  }

  private HttpProxyConfig readHttpProxy(final NestedAttributesMap attributes) {
    HttpProxyConfig config = new HttpProxyConfig();

    config.setHostname(attributes.get("hostname", String.class));
    config.setPort(attributes.get("port", Integer.class));
    if (attributes.contains("authentication")) {
      config.setAuthenticationConfig(readAuthentication(attributes.child("authentication")));
    }
    return config;
  }

  private AuthenticationConfig readAuthentication(final NestedAttributesMap attributes) {
    String type = attributes.get("type", String.class);
    if (type != null) {
      return marshaller(type).unmarshall(attributes);
    }
    return null;
  }
}
