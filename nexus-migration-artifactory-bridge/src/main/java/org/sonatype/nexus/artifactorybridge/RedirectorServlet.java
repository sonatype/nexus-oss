package org.sonatype.nexus.artifactorybridge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

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

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    private static final long serialVersionUID = -1962100850716961287L;

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        LoggerManager logManager = getComponent( LoggerManager.class );
        logger = logManager.getLoggerForComponent( getClass().getName() );

        logger.debug( req.getMethod() + " - " + req.getRequestURI() );

        super.service( req, resp );
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
        URLConnection urlConn = url.openConnection();

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
        }
        catch ( FileNotFoundException e )
        {
            response.sendError( HttpServletResponse.SC_NOT_FOUND );
            return;
        }
        finally
        {
            close( in, out );
        }
    }

    private String getNexusUrl( HttpServletRequest request )
    {
        String nexusUrl = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/nexus";
        return nexusUrl;
    }

    @Override
    protected void doPut( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        InputStream in = null;
        OutputStream out = null;
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
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setRequestMethod( "PUT" );
            urlConn.setDoOutput( true );
            out = urlConn.getOutputStream();
            IOUtils.write( bytes, out );

            urlConn.disconnect();
            int statusCode = urlConn.getResponseCode();
            logger.debug( "URL connection return: " + statusCode + " - " + urlConn.getResponseMessage() );
            response.setStatus( statusCode );
        }
        finally
        {
            close( in, out );
        }

    }

    private void close( InputStream in, OutputStream out )
    {
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
