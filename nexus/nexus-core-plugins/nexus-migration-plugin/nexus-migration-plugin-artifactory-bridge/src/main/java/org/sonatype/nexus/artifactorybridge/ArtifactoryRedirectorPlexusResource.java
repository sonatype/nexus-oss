package org.sonatype.nexus.artifactorybridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ArtifactoryRedirectorPlexusResource.class )
public class ArtifactoryRedirectorPlexusResource
    extends AbstractPlexusResource
    implements ManagedPlexusResource
{

    @Requirement
    private UrlConverter urlConverter;

    public ArtifactoryRedirectorPlexusResource()
    {
        super();

        setReadable( true );

        setModifiable( true );
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/**", "authcBasic,perms[nexus:artifactoryredirect]" );
    }

    @Override
    public String getResourceUri()
    {
        return "";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        // sample artifactory URL
        // http://localhost:8083/artifactory/main-local/nxcm259/released/1.0/released-1.0.pom
        String nexusUrl = request.getHostRef().toString();
        String servletPath = request.getOriginalRef().getPath();
        String nexusPath = urlConverter.convertDownload( servletPath );
        if ( nexusPath == null )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to map artifact request '" + servletPath
                + "'" );
        }

        HttpURLConnection urlConn = null;
        try
        {
            URL url = new URL( nexusUrl + nexusPath );
            getLogger().debug( "Redirecting GET request from:" + servletPath + " to: " + url );

            urlConn = (HttpURLConnection) url.openConnection();

            copyHeaders( request, urlConn );
            String type = urlConn.getContentType();
            return new URLInputStreamRepresentation( type, urlConn.getInputStream(), urlConn );
        }
        catch ( Throwable e )
        {
            int statusCode = getReturnCode( urlConn );
            if ( statusCode == 401 )
            {
                getLogger().debug( "Received 401 from Nexus, inserting Challenge request to the client." );
                response.setChallengeRequest( new ChallengeRequest( ChallengeScheme.HTTP_BASIC,
                                                                    "Sonatype Nexus Repository Manager" ) );
            }
            throw new ResourceException( statusCode, e );
        }
    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        if ( files.size() > 1 )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Expected to have only one file" );
        }
        FileItem file = files.get( 0 );

        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection urlConn = null;
        try
        {

            String nexusUrl = request.getHostRef().toString();
            String servletPath = request.getOriginalRef().getPath();
            String nexusPath = urlConverter.convertDeploy( servletPath );
            if ( nexusPath == null )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to map artifact request '"
                    + servletPath + "'" );
            }

            URL url = new URL( nexusUrl + nexusPath );
            getLogger().debug( "Redirecting PUT request from:" + servletPath + " to: " + url );
            urlConn = (HttpURLConnection) url.openConnection();
            copyHeaders( request, urlConn );

            urlConn.setRequestMethod( "PUT" );
            urlConn.setDoOutput( true );
            out = urlConn.getOutputStream();
            in = file.getInputStream();
            IOUtil.copy( in, out, 8 * 1024 );

            int statusCode = getReturnCode( urlConn );
            if ( !Status.isSuccess( statusCode ) )
            {
                throw new ResourceException( new Status( statusCode ), "URL connection returned: " + statusCode + " - "
                    + urlConn.getResponseMessage() );
            }
            getLogger().debug( "URL connection return: " + statusCode + " - " + urlConn.getResponseMessage() );
        }
        catch ( Throwable e )
        {
            int statusCode = getReturnCode( urlConn );
            if ( statusCode == 401 )
            {
                getLogger().debug( "Received 401 from Nexus, inserting Challenge request to the client." );
                response.setChallengeRequest( new ChallengeRequest( ChallengeScheme.HTTP_BASIC,
                                                                    "Sonatype Nexus Repository Manager" ) );
            }
            throw new ResourceException( statusCode, e );
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }

        return null;
    }

    private int getReturnCode( HttpURLConnection urlConn )
    {
        if ( urlConn != null )
        {
            try
            {
                return urlConn.getResponseCode();
            }
            catch ( IOException e )
            {
                getLogger().debug( "Error retrieving response code", e );
            }
        }
        return 500;
    }

    private void copyHeaders( Request request, HttpURLConnection urlConn )
    {
        Form headers = (Form) request.getAttributes().get( "org.restlet.http.headers" );

        // send headers (authentication)
        for ( Parameter header : headers )
        {
            String name = header.getName();
            String value = header.getValue();
            if ( "Accept-Encoding".equals( name ) )
            { // this do not accept gzip encoding
                continue;
            }
            urlConn.setRequestProperty( name, value );
        }
    }

}
