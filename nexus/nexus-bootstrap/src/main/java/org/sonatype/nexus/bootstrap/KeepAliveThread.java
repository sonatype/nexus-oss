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

package org.sonatype.nexus.bootstrap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread which acts as an endpoint for ping requests.
 *
 * @since 2.2
 */
public class KeepAliveThread
    extends Thread
{

    private static Logger log = LoggerFactory.getLogger( KeepAliveThread.class );

    private final ServerSocket socket;

    private boolean running;

    public KeepAliveThread( final int port )
        throws IOException
    {
        this.socket = new ServerSocket( port, 1, InetAddress.getByName( "127.0.0.1" ) );
        this.running = true;

        setDaemon( true );
        setName( "Keep Alive" );
    }

    @Override
    public void run()
    {
        log.debug( "Listening for ping requests: {}", socket );

        try
        {
            while ( running )
            {
                try
                {
                    final Socket client = socket.accept();
                    log.debug( "Accepted client: {}", client );
                    client.close();
                }
                catch ( Exception e )
                {
                    if ( running )
                    {
                        log.error( "Failed", e );
                    }
                }
            }
        }
        finally
        {
            close( socket );
        }

        log.debug( "Done" );
    }

    public void stopRunning()
    {
        running = false;
        close( socket );
    }

    public int getPort()
    {
        return socket.getLocalPort();
    }

    private static void close( final ServerSocket socket )
    {
        try
        {
            socket.close();
        }
        catch ( IOException ignore )
        {
            // ignore
        }
    }

}
