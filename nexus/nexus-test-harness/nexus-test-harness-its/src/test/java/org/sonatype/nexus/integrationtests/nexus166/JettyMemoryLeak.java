package org.sonatype.nexus.integrationtests.nexus166;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JettyMemoryLeak
{

    private Server server;

    @BeforeMethod
    public void start()
        throws Exception
    {
        JOptionPane.showConfirmDialog( null, "Start" );
        Handler handler = new AbstractHandler()
        {
            private int[] ints = new int[1024 * 1024];

            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                int[] cache = ints;
                ints = null;
                ints = cache;
                cache = null;

                response.setContentType( "text/html" );
                response.setStatus( HttpServletResponse.SC_OK );
                response.getWriter().println( "<h1>Hello</h1>" );
                ( (Request) request ).setHandled( true );
            }
        };

        server = new Server( 8080 );
        server.setHandler( handler );
        server.setStopAtShutdown( true );
        server.start();
    }

    @AfterTest
    public void stop()
        throws Exception
    {
        JOptionPane.showConfirmDialog( null, "Stop" );
        server.stop();
        server = null;
        JOptionPane.showConfirmDialog( null, "Stoped" );
    }

    @Test
    public void runsomething()
    {
        System.out.println( server );
        System.out.println( "In use: " + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )
            / 1024 / 1024 );
    }
}
