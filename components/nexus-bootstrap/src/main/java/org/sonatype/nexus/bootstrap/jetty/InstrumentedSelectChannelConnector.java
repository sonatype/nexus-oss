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
package org.sonatype.nexus.bootstrap.jetty;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Custom re-implementation of {@link com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector}
 * which sets up metrics collection on {@link #doStart()} instead of in CTOR.
 *
 * @since 2.5
 */
public final class InstrumentedSelectChannelConnector
    extends SelectChannelConnector
{
  private static final Logger log = LoggerFactory.getLogger(InstrumentedSelectChannelConnector.class);

  private final MetricRegistry registry;

  private final Clock clock;

  private Timer duration;

  private Meter accepts, connects, disconnects;

  private Counter connections;

  public InstrumentedSelectChannelConnector() {
    registry = SharedMetricRegistries.getOrCreate("nexus");
    clock = Clock.defaultClock();
  }

  @Override
  protected void doStart() throws Exception {
    String port = String.valueOf(getPort());

    this.duration = registry.timer(name(SelectChannelConnector.class, port, "connection-duration"));

    this.accepts = registry.meter(name(SelectChannelConnector.class, port, "accepts"));

    this.connects = registry.meter(name(SelectChannelConnector.class, port, "connects"));

    this.disconnects = registry.meter(name(SelectChannelConnector.class, port, "disconnects"));

    this.connections = registry.counter(name(SelectChannelConnector.class, port, "active-connections"));

    log.info("Metrics enabled");

    super.doStart();
  }

  // TODO: remove metrics on doStop()

  @Override
  public void accept(final int acceptorID) throws IOException {
    super.accept(acceptorID);
    accepts.mark();
  }

  @Override
  protected void connectionOpened(final Connection connection) {
    connections.inc();
    super.connectionOpened(connection);
    connects.mark();
  }

  @Override
  protected void connectionClosed(final Connection connection) {
    super.connectionClosed(connection);
    disconnects.mark();
    final long duration = clock.getTime() - connection.getTimeStamp();
    this.duration.update(duration, TimeUnit.MILLISECONDS);
    connections.dec();
  }
}
