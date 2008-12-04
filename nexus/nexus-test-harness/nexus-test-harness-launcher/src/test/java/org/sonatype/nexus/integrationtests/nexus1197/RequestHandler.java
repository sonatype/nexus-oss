package org.sonatype.nexus.integrationtests.nexus1197;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

public class RequestHandler
    extends AbstractHandler
{

    private String userAgent;

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException, ServletException
    {
        this.userAgent = request.getHeader( "User-Agent" );
    }

    public String getUserAgent()
    {
        return userAgent;
    }

}