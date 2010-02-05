package org.sonatype.nexus.plugins.rrb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

/**
 * A REST resource for retrieving directories from a remote repository. By default, this will automatically be mounted
 * at: http://host:port/nexus/service/local/remotebrowser .
 */
// @Component( role = PlexusResource.class, hint = "protected" )
@Path( "/remotebrowser" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class RemoteBrowserResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{
    // TODO: consider extending AbstractResourceStoreContentPlexusResource

    private final Logger logger = LoggerFactory.getLogger( RemoteBrowserResource.class );

    @Override
    public Object getPayloadInstance()
    {
        // if you allow PUT or POST you would need to return your object.
        return null;
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        // TODO Auto-generated method stub
        super.configureXStream( xstream );
        xstream.alias( "rrbresponse", MavenRepositoryReaderResponse.class );
        xstream.alias( "node", RepositoryDirectory.class );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // Allow anonymous access
        return new PathProtectionDescriptor( this.getResourceUri(), "anon" );
        // should be:
        // return new PathProtectionDescriptor( "/repositories/*/remotebrowser/**",
        // "authcBasic,perms[nexus:remotebrowser]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/remotebrowser";
        // should be:
        // return "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY + "}/remotebrowser/";
        // Changing this would require JavaScript changes
    }

    /**
     * Returns the directory nodes retrieved by remote browsing of proxy repository.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = MavenRepositoryReaderResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {

        // vvvvvvvvvv
        // this logic will be removed when URL is fixed
        String query = request.getResourceRef().getQuery();
        String id = getId( query );
        String remoteUrl = getRemoteUrl( query ); // 
        // ^^^^^^^^^^
        ProxyRepository proxyRepository = null;
        try
        {
            // proxyRepository = getRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );
            proxyRepository = getUnprotectedRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );
        }
        catch ( NoSuchRepositoryException e1 )
        {
            this.logger.warn( "Could not find repository: " + id, e1 );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Could not find repository: " + id, e1 );
        }
        MavenRepositoryReader mr = new MavenRepositoryReader();
        MavenRepositoryReaderResponse data = new MavenRepositoryReaderResponse();
        data.setData( mr.extract( remoteUrl, request.getResourceRef().toString( false, false ), proxyRepository, id ) );
        logger.debug( "return value is {}", data.toString() );
        return data;
    }

    // TODO: if/when xxx is implemented the renderItem method might look something like:
    //    
    // @Inject
    // private NexusItemAuthorizer authorizer;
    //    
    // @Override
    // protected Object renderItem( Context context, Request request, Response response, Variant variant,
    // StorageItem storageItem )
    // throws IOException,
    // AccessDeniedException,
    // NoSuchResourceStoreException,
    // IllegalOperationException,
    // ItemNotFoundException,
    // StorageException,
    // ResourceException
    // {
    //    
    // // I think this is triggered automaticly when you try to get the stream from a fileStorageItem,
    // //but we are not going to call that method. so do the check programaticly
    // if ( !authorizer.authorizePath(
    // getRepositoryRegistry().getRepository( storageItem.getRepositoryId() ),
    // storageItem.getResourceStoreRequest(),
    // Action.read ) )
    // {
    // logger.debug( "No access to: " + storageItem.getResourceStoreRequest().getRequestPath() );
    // throw new AccessDeniedException(
    // storageItem.getResourceStoreRequest(),
    // "No access to file!" );
    // }
    //        
    // // you should be able to get the path from storageItem.getRemoteUrl()
    // // NOTE: we should not use any of the stream methods on the FileStorageItem as that
    // // would cache the remote file( and we are after the directory listings
    // // so I am not even sure how that would work out)
    //        
    // // now you have the remote url so you could feed that into the same thing you are using now
    //        
    // }

    private String getId( String query )
    {
        String result = "";
        int start = query.indexOf( "id=" );
        if ( start != -1 )
        {
            int end = query.indexOf( '&', start );
            if ( end > start )
            {
                result = query.substring( start + 3, end );
            }
            else
            {
                result = query.substring( start + 3 );
            }
        }
        int islocal = result.indexOf( "?isLocal" );
        if ( islocal > 0 )
        {
            result = result.substring( 0, islocal );
        }
        return result;
    }

    private String getRemoteUrl( String query )
    {
        String result = "";
        String prefix = getPrefix( query );
        int start = query.indexOf( "remoteurl=" );
        if ( start != -1 )
        {
            int end = query.indexOf( '&', start );
            if ( end > start )
            {
                result = query.substring( start + 10, end );
            }
            else
            {
                result = query.substring( start + 10 );
            }
        }

        int islocal = result.indexOf( "?" );
        if ( islocal > 0 )
        {
            result = result.substring( 0, islocal );
        }
        // if (!result.endsWith("/")) {
        // result += "/";
        // }
        if ( prefix != "" )
        {
            result = result + "?prefix=" + prefix;
        }

        // decode the param
        try
        {
            result = URLDecoder.decode( result, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "This system does not support the default UTF-8 encoding" );
        }

        logger.debug( "remoter url is {}", result );
        return result;
    }

    private String getPrefix( String query )
    {
        String result = "";
        int start = query.indexOf( "prefix=" );
        if ( start != -1 )
        {
            result = query.substring( start + 7, query.indexOf( "?", start ) );
        }
        return result;
    }
}
