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

  public static final String COMMAND_MONITOR_PORT = CommandMonitorThread.class.getName() + ".port";

  public static final String FIVE_SECONDS = "5000";

  public static final String ONE_SECOND = "1000";

  protected JettyServer server;

  protected Launcher() {
    Logger log = createLogger();
    if (log == null) {
      throw new NullPointerException();
    }
    this.log = log;
  }

  protected Logger createLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  public Integer start(final String[] args) throws Exception {
    if (args.length == 0) {
      log.error("Missing Jetty configuration parameters");
      return 1; // exit
    }

    Configuration config = new Configuration();
    config.load();

    maybeEnableCommandMonitor();
    maybeEnableShutdownIfNotAlive();

    server = new JettyServer(args);
    server.start();

    return null; // continue running
  }

  protected void maybeEnableCommandMonitor() throws IOException {
    String commandMonitorPort = System.getProperty(COMMAND_MONITOR_PORT);
    if (commandMonitorPort == null) {
      commandMonitorPort = System.getenv(COMMAND_MONITOR_PORT);
    }
    if (commandMonitorPort != null) {
      new CommandMonitorThread(
          Integer.parseInt(commandMonitorPort),
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

  protected void maybeEnableShutdownIfNotAlive()
      throws IOException
  {
    String port = System.getProperty(KEEP_ALIVE_PORT);
    if (port == null) {
      port = System.getenv(KEEP_ALIVE_PORT);
    }
    if (port != null) {
      String pingInterval = System.getProperty(KEEP_ALIVE_PING_INTERVAL);
      if (pingInterval == null) {
        pingInterval = System.getenv(KEEP_ALIVE_PING_INTERVAL);
        if (pingInterval == null) {
          pingInterval = FIVE_SECONDS;
        }
      }
      String timeout = System.getProperty(KEEP_ALIVE_TIMEOUT);
      if (timeout == null) {
        timeout = System.getenv(KEEP_ALIVE_TIMEOUT);
        if (timeout == null) {
          timeout = ONE_SECOND;
        }
      }
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
    new Launcher().start(args);
  }
}
