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
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.threads.NexusThreadFactory;

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

    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

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

            pool = Executors.newFixedThreadPool( poolSize, new NexusThreadFactory( "nxhttpproxy", "Nexus HTTP Proxy" ) );

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

    public String getName()
    {
        return "Http Proxy Service Settings";
    }
}
