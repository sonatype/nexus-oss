package org.sonatype.nexus.artifactorybridge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

public class RedirectorServlet
    extends HttpServlet
{
    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    private static final long serialVersionUID = -1962100850716961287L;

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        UrlConverter urlConverter;
        try
        {
            urlConverter = getPlexusContainer().lookup( UrlConverter.class );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "The PlexusServerServlet couldn't lookup the target component (role='"
                + UrlConverter.class + "')", e );
        }

        // sample artifactory URL
        // http://localhost:8083/artifactory/main-local/nxcm259/released/1.0/released-1.0.pom
        String nexusUrl = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/nexus";

        String servletPath = request.getRequestURI();
        String nexusPath = urlConverter.convert( servletPath );
        if ( nexusPath == null )
        {
            response.sendError( 500, "Invalid artifact request '" + servletPath + "'" );
            return;
        }

        URL url = new URL( nexusUrl + nexusPath );
        URLConnection urlConn = url.openConnection();

        try
        {
            InputStream in = urlConn.getInputStream();
            byte[] bytes = IOUtils.toByteArray( in );
            in.close();

            ServletOutputStream out = response.getOutputStream();
            IOUtils.write( bytes, out );
            out.flush();
            out.close();
        }
        catch ( FileNotFoundException e )
        {
            response.sendError( HttpServletResponse.SC_NOT_FOUND );
            return;
        }
    }

}
