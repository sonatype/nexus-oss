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
package org.sonatype.nexus.integrationtests.nexus2862;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.jettytestsuite.BlockingServer;

public class AuthenticationServer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Server server;

    private final List<String> accessedUri;

    private final HashLoginService loginService;

    public List<String> getAccessedUri()
    {
        return accessedUri;
    }

    public AuthenticationServer( Integer port )
    {
        logger.info( "Starting Authentication Server on port {}", port );

        server = new BlockingServer( port );

        Constraint constraint = new Constraint();
        constraint.setName( Constraint.__BASIC_AUTH );
        constraint.setRoles( new String[]{ "user", "admin", "moderator" } );
        constraint.setAuthenticate( true );

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint( constraint );
        cm.setPathSpec( "/*" );

        ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
        loginService = new HashLoginService( "MyRealm" );
        sh.setLoginService( loginService );
        sh.setConstraintMappings( new ConstraintMapping[]{ cm } );
        sh.setStrict( false );

        accessedUri = new ArrayList<String>();

        Handler handler = new AbstractHandler()
        {
            public void handle( String target, Request baseRequest, HttpServletRequest request,
                HttpServletResponse response )
                throws IOException, ServletException
            {
                response.setContentType( "text/html" );
                response.setStatus( HttpServletResponse.SC_OK );
                response.getWriter().println( "<h1>Hello</h1>" );
                ( (Request) request ).setHandled( true );
                accessedUri.add( ( (Request) request ).getUri().toString() );
            }
        };

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[]{ handler, new DefaultHandler() } );

        sh.setHandler( handlers );
        server.setHandler( sh );

    }

    public void start()
        throws Exception
    {
        server.start();
    }

    public void addUser( String name, String password, String... roles )
    {
        loginService.putUser( name, Credential.getCredential( password ), roles );
    }

    public void stop()
        throws Exception
    {
        server.stop();
    }

}
