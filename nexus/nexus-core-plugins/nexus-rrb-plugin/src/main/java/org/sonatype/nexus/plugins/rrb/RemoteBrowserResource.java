/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.rrb;

import java.net.URLDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.ahc.AhcProvider;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.thoughtworks.xstream.XStream;

/**
 * A REST resource for retrieving directories from a remote repository.
 */
@Path( RemoteBrowserResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Component( role = PlexusResource.class, hint = "org.sonatype.nexus.plugins.rrb.RemoteBrowserResource" )
public class RemoteBrowserResource
    extends AbstractResourceStoreContentPlexusResource
    implements PlexusResource
{
    public static final String RESOURCE_URI = "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY
        + "}/remotebrowser";

    @Requirement
    private AhcProvider ahcProvider;

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
        super.configureXStream( xstream );
        xstream.alias( "rrbresponse", MavenRepositoryReaderResponse.class );
        xstream.alias( "node", RepositoryDirectory.class );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // Allow anonymous access for now
        // return new PathProtectionDescriptor( "/repositories/*/remotebrowser/**", "anon" );
        return new PathProtectionDescriptor( "/repositories/*/remotebrowser/**", "authcBasic,perms[nexus:browseremote]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
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
        String remotePath = null;

        try
        {
            remotePath = URLDecoder.decode( storageItem.getRequestPath().substring( 1 ), "UTF-8" );
        }
        catch ( Exception e )
        {
            // old way
            remotePath = storageItem.getRequestPath().substring( 1 );
        }

        ProxyRepository proxyRepository = null;
        AsyncHttpClient client = null;
        try
        {
            proxyRepository = getUnprotectedRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );

            client = getHttpClient( proxyRepository );

            MavenRepositoryReader mr = new MavenRepositoryReader( client );
            MavenRepositoryReaderResponse data = new MavenRepositoryReaderResponse();
            // we really should not do the encoding here, but this is work around until NEXUS-4058 is fixed.
            data.setData( mr.extract( remotePath,
                createRemoteResourceReference( request, id, "" ).toString( false, false ), proxyRepository, id ) );
            logger.debug( "return value is {}", data.toString() );

            return data;
        }
        catch ( NoSuchRepositoryException e )
        {
            this.logger.warn( "Could not find repository: " + id, e );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Could not find repository: " + id, e );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }

    protected AsyncHttpClient getHttpClient( final ProxyRepository proxyRepository )
    {
        final AsyncHttpClientConfig.Builder clientConfigBuilder =
            ahcProvider.getAsyncHttpClientConfigBuilder( proxyRepository, proxyRepository.getRemoteStorageContext() );
        clientConfigBuilder.setFollowRedirects( true );
        clientConfigBuilder.setMaximumNumberOfRedirects( 3 );
        clientConfigBuilder.setMaxRequestRetry( 2 );
        final AsyncHttpClient client = new AsyncHttpClient( clientConfigBuilder.build() );
        return client;
    }

    protected Reference createRemoteResourceReference( Request request, String repoId, String remoteUrl )
    {
        Reference repoRootRef = createRepositoryReference( request, repoId );

        return createReference( repoRootRef, "remotebrowser/" + remoteUrl );
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

    /**
     * DUMMY IMPLEMENTATION, just to satisfy superclass (but why is this class expanding it at all?)
     * 
     * @param request
     * @return
     * @throws NoSuchResourceStoreException
     * @throws ResourceException
     */
    @Override
    protected ResourceStore getResourceStore( final Request request )
        throws NoSuchResourceStoreException, ResourceException
    {
        return getUnprotectedRepositoryRegistry().getRepository(
            request.getAttributes().get( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString() );
    }
}
