/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.proxy.repository;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ConnectHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.sonatype.tests.http.server.jetty.impl.ProxyServlet;

/**
 * An test proxy sever which works also for HTTPS.
 *
 * @since 2.5
 */
public class ProxyServerWithHttpsTunneling
{

    private int port;

    private Server server;

    public int getPort()
    {
        return port;
    }

    public Server getServer()
    {
        return server;
    }

    public void initialize()
    {
        Server proxy = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort( getPort() );
        proxy.addConnector( connector );

        final HandlerCollection handlers = new HandlerCollection();
        proxy.setHandler( handlers );

        final ServletContextHandler context = new ServletContextHandler(
            handlers, "/", ServletContextHandler.SESSIONS
        );
        final ProxyServlet proxyServlet = new ProxyServlet();
        context.addServlet( new ServletHolder( proxyServlet ), "/*" );

        handlers.addHandler( new ConnectHandler() );

        setServer( proxy );
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void setServer( Server server )
    {
        this.server = server;
    }

    public void start()
        throws Exception
    {
        getServer().start();
    }

    public void stop()
        throws Exception
    {
        getServer().stop();
    }

}
