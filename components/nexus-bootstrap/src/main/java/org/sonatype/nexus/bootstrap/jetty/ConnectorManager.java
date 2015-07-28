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
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Jetty's internal connector manager to manage custom {@link ConnectorConfiguration}s.
 * <p/>
 * Note: This class relies on "id" properties in Jetty XML configuration! Hence, the existing beans in Jetty XML
 * should NOT be renamed, while adding new (with unique, non-clashing) IDs is okay thing to do.
 */
public class ConnectorManager
{
  private static final String HTTP_CONFIG_ID = "httpConfig";

  private static final String HTTP_CONNECTOR_ID = "httpConnector";

  private static final String SSL_CONTEXT_FACTORY_ID = "sslContextFactory";

  private static final String HTTPS_CONFIG_ID = "httpsConfig";

  private static final String HTTPS_CONNECTOR_ID = "httpsConnector";

  private final Server server;

  private final Map<String, Object> namedBeans;

  private final IdentityHashMap<ConnectorConfiguration, ServerConnector> managedConnectors;

  public ConnectorManager(final Server server,
                          final Map<String, Object> namedBeans)
  {
    this.server = server;
    this.namedBeans = namedBeans;
    this.managedConnectors = new IdentityHashMap<>();
  }

  /**
   * Adds and starts connector to Jetty based on passed in configuration.
   */
  public ServerConnector addConnector(final ConnectorConfiguration connectorConfiguration)
  {
    verifyConfiguration(connectorConfiguration.getScheme());

    final HttpConfiguration httpConfiguration =
        connectorConfiguration.customize(defaultHttpConfiguration(connectorConfiguration.getScheme()));
    final ServerConnector connectorPrototype =
        defaultConnector(connectorConfiguration.getScheme());
    final ServerConnector serverConnector;
    if (HttpScheme.HTTP == connectorConfiguration.getScheme()) {
      serverConnector = new ServerConnector(
          server,
          connectorPrototype.getAcceptors(),
          connectorPrototype.getSelectorManager().getSelectorCount(),
          new InstrumentedConnectionFactory(
              new HttpConnectionFactory(httpConfiguration)
          )
      );
    }
    else if (HttpScheme.HTTPS == connectorConfiguration.getScheme()) {
      final SslContextFactory sslContextFactory = bean(SSL_CONTEXT_FACTORY_ID, SslContextFactory.class);
      serverConnector = new ServerConnector(
          server,
          connectorPrototype.getAcceptors(),
          connectorPrototype.getSelectorManager().getSelectorCount(),
          new InstrumentedConnectionFactory(
              new SslConnectionFactory(sslContextFactory, "http/1.1")
          ),
          new HttpConnectionFactory(httpConfiguration)
      );
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + connectorConfiguration.getScheme());
    }
    serverConnector.setHost(connectorPrototype.getHost());
    serverConnector.setPort(connectorConfiguration.getPort());
    serverConnector.setIdleTimeout(connectorPrototype.getIdleTimeout());
    serverConnector.setSoLingerTime(connectorPrototype.getSoLingerTime());
    serverConnector.setAcceptorPriorityDelta(connectorPrototype.getAcceptorPriorityDelta());
    serverConnector.setSelectorPriorityDelta(connectorPrototype.getSelectorPriorityDelta());
    serverConnector.setAcceptQueueSize(connectorPrototype.getAcceptQueueSize());

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

  /**
   * Stops and removes the connector configuration from Jetty.
   */
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

  /**
   * Verifies all the needed bits are present in Jetty XML configuration (as HTTPS must be enabled by users).
   */
  private void verifyConfiguration(final HttpScheme scheme) {
    try {
      if (HttpScheme.HTTP == scheme) {
        bean(HTTP_CONFIG_ID, HttpConfiguration.class);
        bean(HTTP_CONNECTOR_ID, ServerConnector.class);
      }
      else if (HttpScheme.HTTPS == scheme) {
        bean(SSL_CONTEXT_FACTORY_ID, SslContextFactory.class);
        bean(HTTPS_CONFIG_ID, HttpConfiguration.class);
        bean(HTTPS_CONNECTOR_ID, ServerConnector.class);
      }
      else {
        throw new IllegalArgumentException("Unsupported HTTP Scheme: " + scheme);
      }
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Jetty HTTPS is not enabled in Nexus", e);
    }
  }

  /**
   * Returns the OOTB defined configuration for given HTTP scheme.
   */
  private HttpConfiguration defaultHttpConfiguration(final HttpScheme httpScheme) {
    if (HttpScheme.HTTP == httpScheme) {
      return bean(HTTP_CONFIG_ID, HttpConfiguration.class);
    }
    else if (HttpScheme.HTTPS == httpScheme) {
      return bean(HTTPS_CONFIG_ID, HttpConfiguration.class);
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + httpScheme);
    }
  }

  /**
   * Returns the OOTB defined connector for given HTTP scheme.
   */
  private ServerConnector defaultConnector(final HttpScheme httpScheme) {
    if (HttpScheme.HTTP == httpScheme) {
      return bean(HTTP_CONNECTOR_ID, ServerConnector.class);
    }
    else if (HttpScheme.HTTPS == httpScheme) {
      return bean(HTTPS_CONNECTOR_ID, ServerConnector.class);
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + httpScheme);
    }
  }

  /**
   * Gets a bean for Jetty XML configuration by name, casted to given type.
   */
  private <T> T bean(final String name, final Class<T> clazz) {
    final Object bean = namedBeans.get(name);
    if (bean == null) {
      throw new IllegalStateException(
          "Jetty XML configuration does not contain bean with name: " + name + ", type=" + clazz.getName());
    }
    return clazz.cast(bean);
  }
}
