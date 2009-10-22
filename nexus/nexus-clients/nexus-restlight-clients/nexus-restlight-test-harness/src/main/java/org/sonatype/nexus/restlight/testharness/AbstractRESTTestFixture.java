package org.sonatype.nexus.restlight.testharness;

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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

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
            int tries = 0;

            // loop until we can't connect to the given port.
            while ( tries < MAX_PORT_TRIES )
            {
                port = ( Math.abs( new Random().nextInt() ) % 63000 ) + 1024;

                logger.info( "(try " + ( tries + 1 ) + "/" + MAX_PORT_TRIES + ") Checking whether port: " + port
                             + " is available..." );

                Socket sock = new Socket();
                sock.setSoTimeout( 1 );
                sock.setSoLinger( true, 1 );

                try
                {
                    sock.connect( new InetSocketAddress( "127.0.0.1", port ) );
                }
                catch ( SocketException e )
                {
                    if ( e.getMessage().indexOf( "Connection refused" ) > -1 )
                    {
                        logger.info( "Port: " + port + " appears to be available!" );
                        break;
                    }
                }
                finally
                {
                    sock.close();
                }

                tries++;
            }

            if ( tries >= MAX_PORT_TRIES )
            {
                throw new IllegalStateException( "Cannot find open port after " + tries + " tries. Giving up." );
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
