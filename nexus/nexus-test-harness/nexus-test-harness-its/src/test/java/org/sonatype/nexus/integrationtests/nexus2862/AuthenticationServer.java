/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus2862;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

public class AuthenticationServer
{

    private final Server server;

    private final List<String> accessedUri;

    private final HashUserRealm userRealm;

    public List<String> getAccessedUri()
    {
        return accessedUri;
    }

    public AuthenticationServer( Integer port )
    {
        server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort( port );
        server.setConnectors( new Connector[] { connector } );

        Constraint constraint = new Constraint();
        constraint.setName( Constraint.__BASIC_AUTH );
        constraint.setRoles( new String[] { "user", "admin", "moderator" } );
        constraint.setAuthenticate( true );

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint( constraint );
        cm.setPathSpec( "/*" );

        SecurityHandler sh = new SecurityHandler();
        userRealm = new HashUserRealm( "MyRealm" );
        sh.setUserRealm( userRealm );
        sh.setConstraintMappings( new ConstraintMapping[] { cm } );

        accessedUri = new ArrayList<String>();

        Handler handler = new AbstractHandler()
        {
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
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
        handlers.setHandlers( new Handler[] { sh, handler, new DefaultHandler() } );

        server.setHandler( handlers );

    }

    public void start()
        throws Exception
    {
        server.start();
    }

    public void addUser( String name, String password, String... roles )
    {
        userRealm.put( name, password );
        for ( String role : roles )
        {
            userRealm.addUserToRole( name, role );
        }
    }

    public void stop()
        throws Exception
    {
        server.stop();
    }

}
