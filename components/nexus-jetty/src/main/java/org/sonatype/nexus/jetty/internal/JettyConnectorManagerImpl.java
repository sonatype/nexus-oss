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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.bootstrap.jetty.InstrumentedConnectionFactory;
import org.sonatype.nexus.bootstrap.jetty.JettyHolder;
import org.sonatype.nexus.jetty.JettyConnectorManager;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.sisu.Dynamic;

import static com.google.common.base.Preconditions.checkState;

/**
 * Default implementation of {@link JettyConnectorManager}.
 */
@Named
@Singleton
public class JettyConnectorManagerImpl
    extends ComponentSupport
    implements JettyConnectorManager
{
  private final JettyHolder jettyHolder;

  @Inject
  public JettyConnectorManagerImpl(@Dynamic final JettyHolder jettyHolder) {
    this.jettyHolder = jettyHolder;
  }

  @Override
  public List<HttpScheme> supportedSchemes() {
    final JettyHolder jettyHolder = verifyJettyStarted();
    final List<HttpScheme> result = new ArrayList<>(2);
    try {
      bean(jettyHolder, "httpConfig", HttpConfiguration.class);
      result.add(HttpScheme.HTTP);
    }
    catch (Exception e) {
    }
    try {
      bean(jettyHolder, "httpsConfig", HttpConfiguration.class);
      result.add(HttpScheme.HTTPS);
    }
    catch (Exception e) {
    }
    return result;
  }

  @Override
  public HttpConfiguration defaultHttpConfiguration(final HttpScheme httpScheme) {
    final JettyHolder jettyHolder = verifyJettyStarted();
    if (HttpScheme.HTTP == httpScheme) {
      return bean(jettyHolder, "httpConfig", HttpConfiguration.class);
    }
    else if (HttpScheme.HTTPS == httpScheme) {
      verifyHttpsEnabled(jettyHolder);
      return bean(jettyHolder, "httpsConfig", HttpConfiguration.class);
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
    final JettyHolder jettyHolder = verifyJettyStarted();
    final ServerConnector serverConnector;
    if (HttpScheme.HTTP == httpScheme) {
      final ConnectionFactory connectionFactory = new InstrumentedConnectionFactory(
          new HttpConnectionFactory(httpConfiguration)
      );
      serverConnector = new ServerConnector(
          jettyHolder.getServer(),
          Integer.valueOf(property(jettyHolder, "http.acceptors", "-1")),
          Integer.valueOf(property(jettyHolder, "http.selectors", "-1")),
          connectionFactory
      );
    }
    else if (HttpScheme.HTTPS == httpScheme) {
      verifyHttpsEnabled(jettyHolder);
      final SslContextFactory sslContextFactory = bean(jettyHolder, "sslContextFactory", SslContextFactory.class);
      final ConnectionFactory connectionFactory = new InstrumentedConnectionFactory(
          new SslConnectionFactory(sslContextFactory, "http/1.1")
      );
      serverConnector = new ServerConnector(
          jettyHolder.getServer(),
          Integer.valueOf(property(jettyHolder, "ssl.acceptors", "-1")),
          Integer.valueOf(property(jettyHolder, "ssl.selectors", "-1")),
          connectionFactory,
          new HttpConnectionFactory(httpConfiguration)
      );
    }
    else {
      throw new IllegalArgumentException("Unsupported HTTP Scheme: " + httpScheme);
    }
    serverConnector.setHost(property(jettyHolder, "application-host", null));
    serverConnector.setPort(port);
    serverConnector.setIdleTimeout(Long.valueOf(property(jettyHolder, "http.tineout", "30000")));
    serverConnector.setSoLingerTime(Integer.valueOf(property(jettyHolder, "http.soLingerTime", "-1")));
    serverConnector.setAcceptorPriorityDelta(Integer.valueOf(property(jettyHolder, "http.acceptorPriorityDelta", "0")));
    serverConnector.setSelectorPriorityDelta(Integer.valueOf(property(jettyHolder, "http.selectorPriorityDelta", "0")));
    serverConnector.setAcceptQueueSize(Integer.valueOf(property(jettyHolder, "http.acceptQueueSize", "0")));
    jettyHolder.getServer().addConnector(serverConnector);
    return serverConnector;
  }

  @Override
  public void removeConnector(final ServerConnector connector) {
    final JettyHolder jettyHolder = verifyJettyStarted();
    jettyHolder.getServer().removeConnector(connector);
  }

  // ==

  private JettyHolder verifyJettyStarted() {
    JettyHolder h = this.jettyHolder;
    checkState(h != null);
    checkState(h.getServer() != null);
    return h;
  }

  private void verifyHttpsEnabled(final JettyHolder jettyHolder) {
    try {
      bean(jettyHolder, "sslContextFactory", SslContextFactory.class);
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Jetty HTTPS is not enabled in Nexus", e);
    }
  }

  private <T> T bean(final JettyHolder jettyHolder, final String name, final Class<T> clazz) {
    final Object bean = jettyHolder.getNamedBeans().get(name);
    if (bean == null) {
      throw new IllegalStateException("Jetty XML configuration does not contain bean with name: " + name);
    }
    return clazz.cast(bean);
  }

  private String property(final JettyHolder jettyHolder, final String name, final String defaultValue) {
    if (jettyHolder.getProperties().containsKey(name)) {
      return jettyHolder.getProperties().get(name);
    }
    else {
      return defaultValue;
    }
  }
}
