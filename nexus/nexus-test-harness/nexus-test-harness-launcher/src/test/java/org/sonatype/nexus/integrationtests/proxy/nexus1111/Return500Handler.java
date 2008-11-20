package org.sonatype.nexus.integrationtests.proxy.nexus1111;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.data.Method;

public class Return500Handler
    extends AbstractHandler
{

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException,
            ServletException
    {

        if ( request.getMethod().equals( Method.HEAD ) )
        {
            response.setContentType( "text/html" );
            response.setStatus( HttpServletResponse.SC_OK );
            response.getWriter().println( "ok" );
            ( (Request) request ).setHandled( true );
        }
        else
        {
            response.setContentType( "text/html" );
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.getWriter().println( "error" );
            ( (Request) request ).setHandled( true );
        }
    }

}
