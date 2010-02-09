package org.sonatype.nexus.plugins.rrb;

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
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
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
    extends AbstractResourceStoreContentPlexusResource
    implements PlexusResource
{

    // TODO: consider extending AbstractResourceStoreContentPlexusResource
    public static final String REMOTE_REPOSITORY_URL = "remoteRepositoryUrl";

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
         return new PathProtectionDescriptor( "/repositories/*/remotebrowser/**", "anon" );
        // should be:
//        return new PathProtectionDescriptor( "/repositories/*/remotebrowser/**",
//                                             "authcBasic,perms[nexus:remotebrowser]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY + "}/remotebrowser";
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

        String id = request.getAttributes().get( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString();
        ResourceStoreRequest storageItem = getResourceStoreRequest( request );
        String remoteUrl = storageItem.getRequestPath().substring( 1 );
        String query = storageItem.getRequestUrl().substring( storageItem.getRequestUrl().indexOf( "?" ) + 1 );
        String prefix = "";
        if ( query.indexOf( "prefix=" ) != -1 )
        {
            int end = query.indexOf( '?', query.indexOf( "prefix=" ) );
            if ( end == -1 )
            {
                end = query.length();
            }
            prefix = "?" + query.substring( query.indexOf( "prefix=" ), end );
        }

        ProxyRepository proxyRepository = null;
        try
        {
            proxyRepository = getUnprotectedRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );
        }
        catch ( NoSuchRepositoryException e1 )
        {
            this.logger.warn( "Could not find repository: " + id, e1 );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Could not find repository: " + id, e1 );
        }
        MavenRepositoryReader mr = new MavenRepositoryReader();
        MavenRepositoryReaderResponse data = new MavenRepositoryReaderResponse();
        data.setData( mr.extract( remoteUrl + prefix, request.getResourceRef().toString( false, false ),
                                  proxyRepository, id ) );
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

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchResourceStoreException, ResourceException
    {
        return getUnprotectedRepositoryRegistry().getRepository(
                                                                 request.getAttributes().get(
                                                                                              AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString() );
    }
}
