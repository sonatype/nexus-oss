/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.artifactorybridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public class RedirectorServlet
    extends HttpServlet
{

    private Logger logger;

    private static final long serialVersionUID = -1962100850716961287L;

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    private <E> E getComponent( Class<E> clazz )
    {
        try
        {
            return getPlexusContainer().lookup( clazz );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "The PlexusServerServlet couldn't lookup the target component (role='"
                + clazz + "')", e );
        }
    }

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        LoggerManager logManager = getComponent( LoggerManager.class );
        logger = logManager.getLoggerForComponent( getClass().getName() );

        logger.debug( req.getMethod() + " - " + req.getRequestURI() );

        super.service( req, resp );
    }

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        UrlConverter urlConverter = getComponent( UrlConverter.class );

        // sample artifactory URL
        // http://localhost:8083/artifactory/main-local/nxcm259/released/1.0/released-1.0.pom
        String nexusUrl = getNexusUrl( request );

        String servletPath = request.getRequestURI();
        String nexusPath = urlConverter.convertDownload( servletPath );
        if ( nexusPath == null )
        {
            response.sendError( 500, "Invalid artifact request '" + servletPath + "'" );
            return;
        }

        URL url = new URL( nexusUrl + nexusPath );
        logger.debug( "Redirecting request to: " + url );

        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

        copyHeaders( request, urlConn );

        InputStream in = null;
        ServletOutputStream out = null;
        try
        {
            in = urlConn.getInputStream();
            byte[] bytes = IOUtils.toByteArray( in );
            in.close();

            out = response.getOutputStream();
            IOUtils.write( bytes, out );
            out.flush();
            out.close();

            response.setStatus( urlConn.getResponseCode() );
        }
        catch ( Throwable e )
        {
            int statusCode = urlConn == null ? 500 : urlConn.getResponseCode();
            response.sendError( statusCode, e.getMessage() );
        }
        finally
        {
            close( urlConn, in, out );
        }
    }

    @Override
    protected void doPut( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection urlConn = null;
        try
        {
            in = request.getInputStream();
            byte[] bytes = IOUtils.toByteArray( in );

            UrlConverter urlConverter = getComponent( UrlConverter.class );
            String nexusUrl = getNexusUrl( request );
            String servletPath = request.getRequestURI();
            String nexusPath = urlConverter.convertDeploy( servletPath );
            if ( nexusPath == null )
            {
                response.sendError( 500, "Invalid artifact request '" + servletPath + "'" );
                return;
            }

            URL url = new URL( nexusUrl + nexusPath );
            urlConn = (HttpURLConnection) url.openConnection();
            copyHeaders( request, urlConn );

            urlConn.setRequestMethod( "PUT" );
            urlConn.setDoOutput( true );
            out = urlConn.getOutputStream();
            IOUtils.write( bytes, out );

            int statusCode = urlConn.getResponseCode();
            logger.debug( "URL connection return: " + statusCode + " - " + urlConn.getResponseMessage() );
            response.setStatus( statusCode );
        }
        catch ( Throwable e )
        {
            int statusCode = urlConn == null ? 500 : urlConn.getResponseCode();
            response.sendError( statusCode, e.getMessage() );
        }
        finally
        {
            close( urlConn, in, out );
        }

    }

    private String getNexusUrl( HttpServletRequest request )
    {
        String nexusUrl = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/nexus";
        return nexusUrl;
    }

    private void copyHeaders( HttpServletRequest request, HttpURLConnection urlConn )
    {
        // send headers (authentication)
        Enumeration<?> headers = request.getHeaderNames();
        while ( headers.hasMoreElements() )
        {
            String name = (String) headers.nextElement();
            String value = request.getHeader( name );
            urlConn.setRequestProperty( name, value );
        }
    }

    private void close( HttpURLConnection urlConn, InputStream in, OutputStream out )
    {
        if ( urlConn != null )
        {
            urlConn.disconnect();
        }

        if ( in != null )
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // just close
            }
        }
        if ( out != null )
        {
            try
            {
                out.close();
            }
            catch ( IOException e )
            {
                // just close
            }
        }
    }
}
