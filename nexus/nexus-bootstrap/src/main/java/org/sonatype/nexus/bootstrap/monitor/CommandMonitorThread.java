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

import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.log.LogProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread which listens for command messages to control the JVM.
 *
 * @since 2.1
 */
public class CommandMonitorThread
    extends Thread
{
    private static final LogProxy log = LogProxy.getLogger( CommandMonitorThread.class );

    private final ServerSocket socket;

    private final Map<String,Command> commands = new HashMap<String, Command>(  );

    public CommandMonitorThread(final int port, final Command... commands) throws IOException {
        if ( commands != null )
        {
            for ( final Command command : commands )
            {
                this.commands.put( command.getId(), command );
            }
        }

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
                log.debug("Accepted client: {}", client);

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String commandId = reader.readLine();
                log.debug("Read command: {}", commandId);
                client.close();

                if ( commandId == null )
                {
                    commandId = PingCommand.PING_COMMAND;
                }
                final Command command = commands.get( commandId );
                if ( command == null )
                {
                    log.error( "Unknown command: {}", commandId );
                }
                else
                {
                    running = !command.execute();
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

        log.info( "Stopped" );
    }

    public int getPort()
    {
        return socket.getLocalPort();
    }

    public static interface Command
    {

        /**
         * ID of command (when it should be executed).
         *
         * @return command id. Never null.
         */
        String getId();

        /**
         * Executes the command.
         *
         * @return true, if command monitor thread should stop running
         */
        boolean execute();

    }

}
