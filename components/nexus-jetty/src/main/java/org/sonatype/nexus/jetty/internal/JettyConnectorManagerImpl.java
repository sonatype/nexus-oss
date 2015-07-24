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
package org.sonatype.nexus.jetty.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.bootstrap.jetty.InstrumentedConnectionFactory;
import org.sonatype.nexus.bootstrap.jetty.JettyHolder;
import org.sonatype.nexus.bootstrap.jetty.JettyHolder.Holder;
import org.sonatype.nexus.jetty.JettyConnectorManager;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Default implementation of {@link JettyConnectorManager}.
 *
 * @since 2.8
 */
@Named
@Singleton
public class JettyConnectorManagerImpl
    extends ComponentSupport
    implements JettyConnectorManager
{
  private final Map<String, String> properties;

  private final Map<String, Object> namedBeans;

  private final Server server;

  @Inject
  public JettyConnectorManagerImpl()
  {
    // TODO: this is same hack as Alin did. This should be exposed via some OSGi voodoo, see LauncherActivator
    final Holder holder = JettyHolder.getHolder();
    this.server = holder.getServer();
    this.properties = holder.getProperties();
    this.namedBeans = holder.getNamedBeans();
  }

  @Override
  public List<HttpScheme> supportedSchemes() {
    final List<HttpScheme> result = new ArrayList<>(2);
    try {
      bean("httpConfig", HttpConfiguration.class);
      result.add(HttpScheme.HTTP);
    }
    catch (Exception e) {
    }
    try {
      bean("httpsConfig", HttpConfiguration.class);
      result.add(HttpScheme.HTTPS);
    }
    catch (Exception e) {
    }
    return result;
  }

  @Override
  public HttpConfiguration defaultHttpConfiguration(final HttpScheme httpScheme) {
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

  @Override
  public ServerConnector addConnector(final HttpScheme httpScheme,
                                      final int port,
                                      final HttpConfiguration httpConfiguration)
  {
    final ServerConnector serverConnector;
    if (HttpScheme.HTTP == httpScheme) {
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
    else if (HttpScheme.HTTPS == httpScheme) {
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
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + httpScheme);
    }
    serverConnector.setHost(property("application-host", null));
    serverConnector.setPort(port);
    serverConnector.setIdleTimeout(Long.valueOf(property("http.tineout", "30000")));
    serverConnector.setSoLingerTime(Integer.valueOf(property("http.soLingerTime", "-1")));
    serverConnector.setAcceptorPriorityDelta(Integer.valueOf(property("http.acceptorPriorityDelta", "0")));
    serverConnector.setSelectorPriorityDelta(Integer.valueOf(property("http.selectorPriorityDelta", "0")));
    serverConnector.setAcceptQueueSize(Integer.valueOf(property("http.acceptQueueSize", "0")));
    server.addConnector(serverConnector);
    return serverConnector;
  }

  @Override
  public void removeConnector(final ServerConnector connector) {
    server.removeConnector(connector);
  }

  // ==

  private void verifyHttpsEnabled() {
    try {
      bean("sslContextFactory", SslContextFactory.class);
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Jetty HTTPS is not enabled in Nexus", e);
    }
  }

  private <T> T bean(String name, Class<T> clazz) {
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
