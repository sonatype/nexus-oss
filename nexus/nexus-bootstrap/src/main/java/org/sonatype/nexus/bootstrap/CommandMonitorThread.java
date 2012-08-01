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
package org.sonatype.nexus.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread which listens for command messages to control the JVM.
 *
 * @since 2.1
 */
public class CommandMonitorThread
    extends Thread
{
    private static final Logger log = LoggerFactory.getLogger(CommandMonitorThread.class);

    private static final String STOP_COMMAND = "STOP";

    private final Launcher launcher;

    private final ServerSocket socket;

    public CommandMonitorThread(final Launcher launcher, final int port) throws IOException {
        if (launcher == null) {
            throw new NullPointerException();
        }
        this.launcher = launcher;
        setDaemon(true);
        setName("Bootstrap Command Monitor");
        // Only listen on local interface
        this.socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
    }

    @Override
    public void run() {
        log.info("Listening for commands: {}", socket);

        boolean running = true;
        while (running) {
            try {
                Socket client = socket.accept();
                log.info("Accepted client: {}", client);

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String command = reader.readLine();
                log.info("Read command: {}", command);
                client.close();

                if (STOP_COMMAND.equals(command)) {
                    log.info("Requesting application stop");
                    launcher.commandStop();
                    running = false;
                }
                else {
                    log.error("Unknown command: {}", command);
                }
            }
            catch (Exception e) {
                log.error("Failed", e);
            }
        }

        try {
            socket.close();
        }
        catch (IOException e) {
            // ignore
        }

        log.info("Stopped");
    }
}
