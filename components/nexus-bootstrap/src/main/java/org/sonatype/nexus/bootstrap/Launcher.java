/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.bootstrap;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.bootstrap.jetty.JettyServer;
import org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread;
import org.sonatype.nexus.bootstrap.monitor.KeepAliveThread;
import org.sonatype.nexus.bootstrap.monitor.commands.ExitCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.HaltCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopApplicationCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread.LOCALHOST;
import static org.sonatype.nexus.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_PING_INTERVAL;
import static org.sonatype.nexus.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_PORT;
import static org.sonatype.nexus.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_TIMEOUT;

/**
 * Nexus bootstrap launcher.
 *
 * @since 2.1
 */
public class Launcher
{
  protected final Logger log;

  public static final InheritableThreadLocal<Map<String,String>> PROPERTIES = new InheritableThreadLocal<>();

  // FIXME: Move this to CommandMonitorThread
  public static final String COMMAND_MONITOR_PORT = CommandMonitorThread.class.getName() + ".port";

  private static final String FIVE_SECONDS = "5000";

  private static final String ONE_SECOND = "1000";

  private final JettyServer server;

  public Launcher(final @Nullable ClassLoader classLoader,
                  final @Nullable Map<String,String> properties,
                  final String[] args)
      throws Exception
  {
    Logger log = createLogger();
    if (log == null) {
      throw new NullPointerException();
    }
    this.log = log;

    ClassLoader cl = (classLoader == null) ? getClass().getClassLoader() : classLoader;

    PropertyMap props = new PropertyMap();
    if (properties == null) {
      Configuration config = new Configuration();
      config.load();
      props.putAll(config.getProperties());
    }
    else {
      props.putAll(properties);
    }
    PROPERTIES.set(props);

    if (args == null) {
      throw new NullPointerException();
    }
    if (args.length == 0) {
      throw new IllegalArgumentException("Missing args");
    }

    this.server = new JettyServer(cl, props, args);
  }

  protected Logger createLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  public void start() throws Exception {
    maybeEnableCommandMonitor();
    maybeEnableShutdownIfNotAlive();

    server.start();
  }

  protected String getProperty(final String name, final String defaultValue) {
    String value = System.getProperty(name, System.getenv(name));
    if (value == null) {
      value = defaultValue;
    }
    return value;
  }

  protected void maybeEnableCommandMonitor() throws IOException {
    String port = getProperty(COMMAND_MONITOR_PORT, null);
    if (port != null) {
      new CommandMonitorThread(
          Integer.parseInt(port),
          new StopApplicationCommand(new Runnable()
          {
            @Override
            public void run() {
              Launcher.this.commandStop();
            }
          }),
          new PingCommand(),
          new ExitCommand(),
          new HaltCommand()
      ).start();
    }
  }

  protected void maybeEnableShutdownIfNotAlive() throws IOException {
    String port = getProperty(KEEP_ALIVE_PORT, null);
    if (port != null) {
      String pingInterval = getProperty(KEEP_ALIVE_PING_INTERVAL, FIVE_SECONDS);
      String timeout = getProperty(KEEP_ALIVE_TIMEOUT, ONE_SECOND);

      new KeepAliveThread(
          LOCALHOST,
          Integer.parseInt(port),
          Integer.parseInt(pingInterval),
          Integer.parseInt(timeout)
      ).start();
    }
  }

  public void commandStop() {
    ShutdownHelper.exit(0);
  }

  public void stop() throws Exception {
    server.stop();
  }

  public static void main(final String[] args) throws Exception {
    new Launcher(null, null, args).start();
  }
}
