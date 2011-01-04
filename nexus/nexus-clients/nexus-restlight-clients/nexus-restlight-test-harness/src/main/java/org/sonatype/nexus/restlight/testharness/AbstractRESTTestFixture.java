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
package org.sonatype.nexus.restlight.testharness;

import java.net.ServerSocket;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

/**
 * Base implementation for {@link RESTTestFixture} that supplies the methods for managing and retrieving information
 * about the test-harness HTTP {@link Server} instance, the debug flag, and basic expectations about the expected client
 * request headers. Additionally, this base class manages the response headers which will be injected into the response
 * if the client request validates.
 */
public abstract class AbstractRESTTestFixture
    implements RESTTestFixture
{

    private static final int MAX_PORT_TRIES = 10;

    private static final String TEST_PORT_SYSPROP = "test.port";

    private Server server;

    private int port;

    private boolean debugEnabled;

    private String authUser;

    private String authPassword;

    protected AbstractRESTTestFixture( final String user, final String password )
    {
        this.authUser = user;
        this.authPassword = password;
    }

    /**
     * {@inheritDoc}
     */
    public Server getServer()
    {
        return server;
    }

    /**
     * {@inheritDoc}
     */
    public int getPort()
    {
        return port;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setDebugEnabled( final boolean debugEnabled )
    {
        this.debugEnabled = debugEnabled;
    }

    protected void setupLogging()
    {
        if ( !LogManager.getRootLogger().getAllAppenders().hasMoreElements() )
        {
            LogManager.getRootLogger().addAppender( new ConsoleAppender( new SimpleLayout() ) );
        }

        if ( isDebugEnabled() )
        {
            LogManager.getRootLogger().setLevel( Level.DEBUG );
        }
        else
        {
            LogManager.getRootLogger().setLevel( Level.INFO );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startServer()
        throws Exception
    {
        setupLogging();

        Logger logger = LogManager.getLogger( getClass() );

        String portStr = System.getProperty( TEST_PORT_SYSPROP );

        if ( portStr != null )
        {
            port = Integer.parseInt( portStr );
            logger.info( "Using port: " + port + ", given by system property '" + TEST_PORT_SYSPROP + "'." );
        }
        else
        {
            logger.info( "Randomly looking for an open port..." );

            ServerSocket ss = new ServerSocket( 0 );
            try
            {
                port = ss.getLocalPort();
            }
            finally
            {
                ss.close();
            }
        }

        logger.info( "Starting test server on port: " + port );

        server = new Server( port );

        Constraint constraint = new Constraint();

        constraint.setRoles( new String[] { "allowed" } );
        constraint.setAuthenticate( true );

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint( constraint );
        cm.setPathSpec( "/*" );

        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setAuthMethod( NxBasicAuthenticator.AUTH_TYPE );
        securityHandler.setAuthenticator( new NxBasicAuthenticator() );

        HashUserRealm securityRealm = new HashUserRealm( "Nexus REST Test Fixture" );

        securityRealm.put( authUser, authPassword );
        securityRealm.addUserToRole( authUser, "allowed" );

        securityHandler.setUserRealm( securityRealm );
        securityHandler.setConstraintMappings( new ConstraintMapping[] { cm } );

        HandlerWrapper wrapper = new HandlerWrapper();
        wrapper.addHandler( getTestHandler() );
        wrapper.addHandler( securityHandler );

        server.setHandler( wrapper );

        server.start();
    }

    /**
     * {@inheritDoc}
     */
    public void stopServer()
        throws Exception
    {
        LogManager.getLogger( getClass() ).info( "Stopping test server." );

        if ( server != null && server.isStarted() )
        {
            server.stop();
        }
    }

    public String getAuthUser()
    {
        return authUser;
    }

    public void setAuthUser( final String authUser )
    {
        this.authUser = authUser;
    }

    public String getAuthPassword()
    {
        return authPassword;
    }

    public void setAuthPassword( final String authPassword )
    {
        this.authPassword = authPassword;
    }

}
