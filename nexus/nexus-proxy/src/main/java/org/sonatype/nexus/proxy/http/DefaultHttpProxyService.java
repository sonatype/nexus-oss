/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;

/**
 * A default HTTP Proxy service. A very simple network service based on Java 5 ExecutorService.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultHttpProxyService
    extends AbstractLogEnabled
    implements HttpProxyService, Initializable, EventListener
{
    public static final int DEFAULT_TIMEOUT = 20 * 1000;

    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * @plexus.requirement
     */
    private NexusURLResolver nexusURLResolver;

    /**
     * @plexus.configuration default-value="10"
     */
    private int poolSize;

    private int port;

    private ServerSocket serverSocket;

    private ExecutorService pool;

    private Thread serverThread;

    private boolean running;

    private HttpProxyPolicy httpProxyPolicy = HttpProxyPolicy.STRICT;

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            httpProxyPolicy = HttpProxyPolicy.fromModel( applicationConfiguration
                .getConfiguration().getHttpProxy().getProxyPolicy() );

            if ( port != applicationConfiguration.getConfiguration().getHttpProxy().getPort() )
            {
                port = applicationConfiguration.getConfiguration().getHttpProxy().getPort();

                if ( running )
                {
                    stopService();

                    startService();
                }
            }
        }
    }

    public void startService()
    {
        if ( running )
        {
            return;
        }

        try
        {
            running = true;

            serverSocket = new ServerSocket( port );

            pool = Executors.newFixedThreadPool( poolSize );

            serverThread = new Thread( new Server( this ) );

            serverThread.start();

            getLogger().info( "HttpProxy service started on port " + port );
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot start HttpProxy service:", e );

            stopService();
        }
    }

    public void stopService()
    {
        getLogger().info( "HttpProxy service stopped." );

        running = false;

        if ( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch ( IOException e )
            {
                getLogger().warn( "Exception while stopping HttpProxy service:", e );
            }
        }

        if ( serverThread != null )
        {
            serverThread.interrupt();
        }

        if ( pool != null )
        {
            pool.shutdownNow();
        }
    }

    public NexusURLResolver getNexusURLResolver()
    {
        return nexusURLResolver;
    }

    protected class Server
        implements Runnable
    {
        private final HttpProxyService service;

        private final Logger handlerLogger;

        public Server( HttpProxyService service )
        {
            this.service = service;

            this.handlerLogger = getLogger().getChildLogger( "handler" );
        }

        public void run()
        {
            try
            {
                while ( running )
                {
                    Socket socket = serverSocket.accept();

                    HttpProxyHandler handler = new HttpProxyHandler( handlerLogger, service, httpProxyPolicy, socket );

                    pool.execute( handler );
                }
            }
            catch ( IOException e )
            {
            }
        }
    }
}
