/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CHttpProxyCoreConfiguration;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;

/**
 * A default HTTP Proxy service. A very simple network service based on Java 5 ExecutorService.
 * 
 * @author cstamas
 */
@Component( role = HttpProxyService.class )
public class DefaultHttpProxyService
    extends AbstractConfigurable
    implements HttpProxyService, Startable
{
    public static final int DEFAULT_TIMEOUT = 20 * 1000;

    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private NexusURLResolver nexusURLResolver;

    @Configuration( value = "10" )
    private int poolSize;

    private ServerSocket serverSocket;

    private ExecutorService pool;

    private Thread serverThread;

    private volatile boolean running;

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ).isEnabled();
    }

    public void setEnabled( boolean enabled )
    {
        getCurrentConfiguration( true ).setEnabled( enabled );
    }

    public int getPort()
    {
        return getCurrentConfiguration( false ).getPort();
    }

    public void setPort( int port )
    {
        getCurrentConfiguration( true ).setPort( port );
    }

    public HttpProxyPolicy getHttpProxyPolicy()
    {
        String policyStr = getCurrentConfiguration( false ).getProxyPolicy();

        if ( StringUtils.isBlank( policyStr ) )
        {
            policyStr = CHttpProxySettings.PROXY_POLICY_STRICT;
        }

        return HttpProxyPolicy.fromModel( policyStr );
    }

    public void setHttpProxyPolicy( HttpProxyPolicy httpProxyPolicy )
    {
        getCurrentConfiguration( true ).setProxyPolicy( HttpProxyPolicy.toModel( httpProxyPolicy ) );
    }

    // ==

    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected CHttpProxySettings getCurrentConfiguration( boolean forWrite )
    {
        return ( (CHttpProxyCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CHttpProxyCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    @Override
    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean wasDirty = super.commitChanges();

        if ( wasDirty )
        {
            stop();

            start();
        }

        return wasDirty;
    }

    // ==

    public synchronized void start()
    {
        if ( running || !isEnabled() )
        {
            running = isEnabled();

            return;
        }

        try
        {
            running = true;

            serverSocket = new ServerSocket( getPort() );

            pool = Executors.newFixedThreadPool( poolSize );

            serverThread = new Thread( new Server( this ) );

            serverThread.start();

            getLogger().info( "HttpProxy service started on port " + getPort() );
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot start HttpProxy service:", e );

            stop();
        }
    }

    public synchronized void stop()
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

                    HttpProxyHandler handler =
                        new HttpProxyHandler( handlerLogger, service, getHttpProxyPolicy(), socket );

                    pool.execute( handler );
                }
            }
            catch ( IOException e )
            {
            }
        }
    }
}
