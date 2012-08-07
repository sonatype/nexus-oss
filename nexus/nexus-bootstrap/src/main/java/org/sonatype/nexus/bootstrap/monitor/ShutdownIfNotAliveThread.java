/*
 * Copyright (c) 2007-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.bootstrap.monitor;

import static org.sonatype.nexus.bootstrap.monitor.commands.PingCommand.PING_COMMAND;

import java.io.IOException;
import java.net.ConnectException;

import org.sonatype.nexus.bootstrap.log.LogProxy;

/**
 * Thread which pings a specified host/port at a configured interval and runs a shutdown coe in case that there is no
 * response (connection refused).
 *
 * @since 2.2
 */
public class ShutdownIfNotAliveThread
    extends Thread
{

    /**
     * Logger. Uses log proxy to be able to redirect log output to System.out if SLF4J is not available (Nexus < 2.1).
     */
    private static LogProxy log = LogProxy.getLogger( ShutdownIfNotAliveThread.class );

    /**
     * Runnable to executed in case that keep alive determines that it should shutdown Nexus.
     */
    private final Runnable shutdown;

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
     * Command monitor talker used to ping the configured port.
     * Never nul.
     */
    private final CommandMonitorTalker talker;

    /**
     * Constructor.
     *
     * @param shutdown     shutdown code to be run in case there is no ping response (connection refused)
     * @param host         host to be pinged
     * @param port         port on host to be pinged
     * @param pingInterval interval between pings
     * @param timeout      ping timeout
     * @throws IOException Re-thrown from creating an {@link CommandMonitorTalker}
     */
    public ShutdownIfNotAliveThread( final Runnable shutdown,
                                     final String host,
                                     final int port,
                                     final int pingInterval,
                                     final int timeout )
        throws IOException
    {
        if ( shutdown == null )
        {
            throw new NullPointerException();
        }
        this.shutdown = shutdown;
        this.pingInterval = pingInterval;
        this.timeout = timeout;

        this.talker = new CommandMonitorTalker( host, port );

        this.running = true;
        this.setDaemon( true );
        setName( "Shutdown if not alive" );
    }

    /**
     * Continue pinging on configured port until there is a connection (refused) exception, case when a shutdown will be
     * performed.
     */
    @Override
    public void run()
    {
        log.info(
            "Shutdown thread pinging {} on port {} every {} milliseconds",
            talker.getHost(), talker.getPort(), pingInterval
        );

        while ( running )
        {
            try
            {
                try
                {
                    ping();
                    sleep( pingInterval );
                }
                catch ( final InterruptedException ignore )
                {
                    ping();
                }
            }
            catch ( ConnectException e )
            {
                stopRunning();
                shutdown();
            }
        }

        log.debug( "Stopped" );
    }

    /**
     * Pings the configured host/port.
     *
     * @throws ConnectException If ping fails
     */
    private void ping()
        throws ConnectException
    {
        try
        {
            log.debug( "Pinging {} on port {} ...", talker.getHost(), talker.getPort() );
            talker.send( PING_COMMAND, timeout );
        }
        catch ( ConnectException e )
        {
            log.warn( "Exception got while pinging {}:{}", e.getClass().getName(), e.getMessage() );
            throw e;
        }
        catch ( Exception ignore )
        {
            log.info( "Skipping exception got while pinging {}:{}", ignore.getClass().getName(), ignore.getMessage() );
        }
    }

    /**
     * Runs the shutdown code.
     */
    // @TestAccessible
    void shutdown()
    {
        log.warn( "Shutting down as there is no ping response from {} on port {}", talker.getHost(), talker.getPort() );
        shutdown.run();
    }

    /**
     * Stops this thread from running (without running the shutdown code).
     */
    public void stopRunning()
    {
        running = false;
    }

}
