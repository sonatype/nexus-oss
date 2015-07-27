/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bootstrap.jetty;

import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Jetty's internal connector manager to manage custom {@link ConnectorConfiguration}s.
 */
public class ConnectorManager
{
  private final Server server;

  private final Map<String, String> properties;

  private final Map<String, Object> namedBeans;

  private final IdentityHashMap<ConnectorConfiguration, ServerConnector> managedConnectors;

  public ConnectorManager(final Server server,
                          final Map<String, String> properties,
                          final Map<String, Object> namedBeans)
  {
    this.server = server;
    this.properties = properties;
    this.namedBeans = namedBeans;
    this.managedConnectors = new IdentityHashMap<>();
  }

  public ServerConnector addConnector(final ConnectorConfiguration connectorConfiguration)
  {
    final HttpConfiguration httpConfiguration =
        connectorConfiguration.customize(defaultHttpConfiguration(connectorConfiguration.getScheme()));

    final ServerConnector serverConnector;
    if (HttpScheme.HTTP == connectorConfiguration.getScheme()) {
      final ConnectionFactory connectionFactory = new InstrumentedConnectionFactory(
          new HttpConnectionFactory(httpConfiguration)
      );
      serverConnector = new ServerConnector(
          server,
          Integer.valueOf(property("http.acceptors", "-1")),
          Integer.valueOf(property("http.selectors", "-1")),
          connectionFactory
      );
    }
    else if (HttpScheme.HTTPS == connectorConfiguration.getScheme()) {
      verifyHttpsEnabled();
      final SslContextFactory sslContextFactory = bean("sslContextFactory", SslContextFactory.class);
      final ConnectionFactory connectionFactory = new InstrumentedConnectionFactory(
          new SslConnectionFactory(sslContextFactory, "http/1.1")
      );
      serverConnector = new ServerConnector(
          server,
          Integer.valueOf(property("ssl.acceptors", "-1")),
          Integer.valueOf(property("ssl.selectors", "-1")),
          connectionFactory,
          new HttpConnectionFactory(httpConfiguration)
      );
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + connectorConfiguration.getScheme());
    }
    serverConnector.setHost(property("application-host", null));
    serverConnector.setPort(connectorConfiguration.getPort());
    serverConnector.setIdleTimeout(Long.valueOf(property("http.tineout", "30000")));
    serverConnector.setSoLingerTime(Integer.valueOf(property("http.soLingerTime", "-1")));
    serverConnector.setAcceptorPriorityDelta(Integer.valueOf(property("http.acceptorPriorityDelta", "0")));
    serverConnector.setSelectorPriorityDelta(Integer.valueOf(property("http.selectorPriorityDelta", "0")));
    serverConnector.setAcceptQueueSize(Integer.valueOf(property("http.acceptQueueSize", "0")));

    managedConnectors.put(connectorConfiguration, serverConnector);
    server.addConnector(serverConnector);
    try {
      serverConnector.start();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    return serverConnector;
  }

  public void removeConnector(final ConnectorConfiguration connectorConfiguration) {
    final ServerConnector serverConnector = managedConnectors.remove(connectorConfiguration);
    if (serverConnector != null) {
      try {
        serverConnector.stop();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      server.removeConnector(serverConnector);
    }
  }

  // ==

  private HttpConfiguration defaultHttpConfiguration(final HttpScheme httpScheme) {
    if (HttpScheme.HTTP == httpScheme) {
      return bean("httpConfig", HttpConfiguration.class);
    }
    else if (HttpScheme.HTTPS == httpScheme) {
      verifyHttpsEnabled();
      return bean("httpsConfig", HttpConfiguration.class);
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + httpScheme);
    }
  }

  private void verifyHttpsEnabled() {
    try {
      bean("sslContextFactory", SslContextFactory.class);
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Jetty HTTPS is not enabled in Nexus", e);
    }
  }

  private <T> T bean(final String name, final Class<T> clazz) {
    final Object bean = namedBeans.get(name);
    if (bean == null) {
      throw new IllegalStateException("Jetty XML configuration does not contain bean with name: " + name);
    }
    return clazz.cast(bean);
  }

  private String property(final String name, final String defaultValue) {
    if (properties.containsKey(name)) {
      return properties.get(name);
    }
    else {
      return defaultValue;
    }
  }
}
