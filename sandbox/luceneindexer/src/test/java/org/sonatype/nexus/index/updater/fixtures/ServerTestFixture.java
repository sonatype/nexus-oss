package org.sonatype.nexus.index.updater.fixtures;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerTestFixture
{

    private static final String SERVER_ROOT_RESOURCE_PATH = "index-updater/server-root";

    private static final String SIXTY_TWO_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static final String LONG_PASSWORD = SIXTY_TWO_CHARS + SIXTY_TWO_CHARS;

    private final Server server;

    public ServerTestFixture( final int port )
        throws Exception
    {
        server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort( port );

        server.setConnectors( new Connector[] { connector } );

        Constraint constraint = new Constraint();
        constraint.setName( Constraint.__BASIC_AUTH );

        constraint.setRoles( new String[] { "allowed" } );
        constraint.setAuthenticate( true );

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint( constraint );
        cm.setPathSpec( "/protected/*" );

        SecurityHandler sh = new SecurityHandler();

        HashUserRealm realm = new HashUserRealm( "POC Server" );
        realm.put( "user", "password" );
        realm.put( "longuser", LONG_PASSWORD );
        realm.addUserToRole( "user", "allowed" );
        realm.addUserToRole( "longuser", "allowed" );

        sh.setUserRealm( realm );
        sh.setConstraintMappings( new ConstraintMapping[] { cm } );

        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath( "/" );

        File base = getBase();
        ctx.setWar( base.getAbsolutePath() );
        ctx.addHandler( sh );

        ctx.getServletHandler().addServletWithMapping( TimingServlet.class, "/slow/*" );
        ctx.getServletHandler().addServletWithMapping( InfiniteRedirectionServlet.class, "/redirect-trap/*" );

        SessionHandler sessionHandler = ctx.getSessionHandler();
        ( (AbstractSessionManager) sessionHandler.getSessionManager() ).setUsingCookies( false );

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers( new Handler[] { ctx, new DefaultHandler() } );

        server.setHandler( handlers );
        server.start();
    }

    private static File getBase()
        throws URISyntaxException
    {
        URL resource = Thread.currentThread().getContextClassLoader().getResource( SERVER_ROOT_RESOURCE_PATH );
        if ( resource == null )
        {
            throw new IllegalStateException( "Cannot find classpath resource: " + SERVER_ROOT_RESOURCE_PATH );
        }

        return new File( resource.toURI().normalize() );
    }

    public void stop()
        throws Exception
    {
        server.stop();
        server.join();
    }

    public static final class TimingServlet
        extends HttpServlet
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
        {
            String basePath = req.getServletPath();
            String subPath = req.getRequestURI().substring( basePath.length() );

            File base;
            try
            {
                base = getBase();
            }
            catch ( URISyntaxException e )
            {
                resp.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Cannot find server document root in classpath: " + SERVER_ROOT_RESOURCE_PATH );
                return;
            }

            File f = new File( base, "slow" + subPath );
            InputStream in = null;
            try
            {
                in = new FileInputStream( f );
                OutputStream out = resp.getOutputStream();

                int read = -1;
                byte[] buf = new byte[64];
                while ( ( read = in.read( buf ) ) > -1 )
                {
                    System.out.println( "Sending " + read + " bytes (after pausing 1 seconds)..." );
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException e )
                    {
                    }

                    out.write( buf, 0, read );
                }

                out.flush();
            }
            finally
            {
                if ( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        }
    }

    public static final class InfiniteRedirectionServlet
        extends HttpServlet
    {
        private static final long serialVersionUID = 1L;

        static int redirCount = 0;

        @Override
        protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
        {
            String path = req.getServletPath();
            String subPath = req.getRequestURI().substring( path.length() );

            path += subPath + "-" + ( ++redirCount );
            resp.sendRedirect( path );
        }
    }

}
