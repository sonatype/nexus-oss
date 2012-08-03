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

import org.sonatype.nexus.bootstrap.log.LogProxy;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Talks to the command monitor.
 *
 * @since 2.1
 */
public class CommandMonitorTalker
{
    private static LogProxy log = LogProxy.getLogger( CommandMonitorTalker.class );

    public static final String LOCALHOST = "127.0.0.1";

    private final String host;

    private final int port;

    public CommandMonitorTalker(final String host, final int port) {
        if (host == null) {
            throw new NullPointerException();
        }
        this.host = host;
        if (port < 1) {
            throw new IllegalArgumentException("Invalid port");
        }
        this.port = port;
    }

    public void send(final String command) throws Exception {
        if (command == null) {
            throw new NullPointerException();
        }

        log.debug("Sending command: {}", command);

        Socket socket = new Socket();
        socket.setSoTimeout(5000);
        socket.connect(new InetSocketAddress(host, port));
        try {
            OutputStream output = socket.getOutputStream();
            output.write(command.getBytes());
            output.close();
        }
        finally {
            socket.close();
        }
    }
}
