/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.bootstrap.monitor;

import java.io.IOException;
import java.net.ConnectException;

import static org.sonatype.nexus.bootstrap.monitor.commands.PingCommand.PING_COMMAND;

/**
 * Thread which pings a specified host/port at a configured interval and halts the jvm if the remote monitor is unreachable.
 *
 * @since 2.2
 */
public class HaltIfRemoteMonitorUnreachableThread
    extends Thread
{
    // NOTE: Avoiding any logging our sysout usage by this class, this could lockup logging when its detected a failure and attempting to shutdown

    /**
     * Interval between pinging.
     */
    private int pingInterval;

    /**
     * Ping timeout.
     */
    private int timeout;

    /**
     * True if this thread should continue running.
     */
    private boolean running;

    /**
     * Command monitor talker used to ping the configured port. Never nul.
     */
    private final CommandMonitorTalker talker;

    /**
     * @param host         host to be pinged
     * @param port         port on host to be pinged
     * @param pingInterval interval between pings
     * @param timeout      ping timeout
     */
    public HaltIfRemoteMonitorUnreachableThread(final String host,
                                                final int port,
                                                final int pingInterval,
                                                final int timeout)
        throws IOException
    {
        setDaemon(true);
        setName(getClass().getName());

        this.pingInterval = pingInterval;
        this.timeout = timeout;
        this.talker = new CommandMonitorTalker(host, port);
        this.running = true;
    }

    /**
     * Continue pinging on configured port until there is a connection (refused) exception, case when a shutdown will be performed.
     */
    @Override
    public void run() {
        while (running) {
            try {
                try {
                    ping();
                    sleep(pingInterval);
                }
                catch (final InterruptedException ignore) {
                    ping();
                }
            }
            catch (ConnectException e) {
                stopRunning();
                halt();
            }
        }
    }

    /**
     * Pings the configured host/port.
     *
     * @throws ConnectException If ping fails
     */
    private void ping() throws ConnectException {
        try {
            talker.send(PING_COMMAND, timeout);
        }
        catch (ConnectException e) {
            throw e;
        }
        catch (Exception e) {
            // ignore
        }
    }

    // @TestAccessible
    void halt() {
        Runtime.getRuntime().halt(666);
    }

    /**
     * Stops this thread from running (without running the shutdown code).
     */
    public void stopRunning() {
        running = false;
    }
}
