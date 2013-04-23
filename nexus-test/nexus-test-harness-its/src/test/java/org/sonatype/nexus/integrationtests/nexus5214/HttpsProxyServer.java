/*
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
package org.sonatype.nexus.integrationtests.nexus5214;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ConnectHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.sonatype.jettytestsuite.proxy.MonitorableProxyServlet;

/**
 * An test proxy sever (copied from {@link org.sonatype.jettytestsuite.ProxyServer}) but which works also for HTTPS.
 *
 * @since 2.5
 */
public class HttpsProxyServer
    implements Initializable, Startable
{

    private ServletContextHandler context;

    /**
     * The port.
     */
    private int port;

    private MonitorableProxyServlet proxyServlet;

    /**
     * The server.
     */
    private Server server;

    public List<String> getAccessedUris()
    {
        if ( proxyServlet == null )
        {
            return null;
        }

        return proxyServlet.getAccessedUris();
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    public ServletContextHandler getProxyingContext()
    {
        return context;
    }

    public MonitorableProxyServlet getProxyServlet()
    {
        return proxyServlet;
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public Server getServer()
    {
        return server;
    }

    public String getUrl( String context )
    {
        return "http://localhost:" + getPort() + "/" + context;
    }

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        Server proxy = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort( getPort() );
        proxy.addConnector( connector );

        final HandlerCollection handlers = new HandlerCollection();
        proxy.setHandler( handlers );

        context = new ServletContextHandler( handlers, "/", ServletContextHandler.SESSIONS );
        proxyServlet = new MonitorableProxyServlet();
        context.addServlet( new ServletHolder( proxyServlet ), "/*" );

        handlers.addHandler( new ConnectHandler() );

        setServer( proxy );
    }

    /**
     * Sets the port.
     *
     * @param port the new port
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    // ===
    // Initializable iface

    /**
     * Sets the server.
     *
     * @param server the new server
     */
    public void setServer( Server server )
    {
        this.server = server;
    }

    // ===
    // Startable iface

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#start()
     */
    public void start()
        throws StartingException
    {
        try
        {
            getServer().start();
        }
        catch ( Exception e )
        {
            throw new StartingException( "Error starting embedded Jetty server.", e );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#stop()
     */
    public void stop()
        throws StoppingException
    {
        try
        {
            getServer().stop();
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Error stopping embedded Jetty server.", e );
        }
    }

    // ===
    // Private stuff

}
