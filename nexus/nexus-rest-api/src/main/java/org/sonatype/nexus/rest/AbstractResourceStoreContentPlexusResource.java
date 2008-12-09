/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.security.filter.authc.NexusHttpAuthenticationFilter;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;

import com.noelios.restlet.ext.servlet.ServletCall;
import com.noelios.restlet.http.HttpRequest;

/**
 * This is an abstract resource handler that uses ResourceStore implementor and publishes those over REST.
 * 
 * @author cstamas
 */
public abstract class AbstractResourceStoreContentPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String IS_LOCAL_PARAMETER = "isLocal";

    public AbstractResourceStoreContentPlexusResource()
    {
        super();

        setReadable( true );

        setModifiable( true );
    }

    public boolean acceptsUpload()
    {
        return true;
    }

    protected String getResourceStorePath( Request request )
    {
        return parsePathFromUri( request.getResourceRef().getRemainingPart() );
    }

    protected boolean isLocal( Request request, String resourceStorePath )
    {
        // check do we need local only access
        boolean isLocal = request.getResourceRef().getQueryAsForm().getFirst( IS_LOCAL_PARAMETER ) != null;

        // overriding isLocal is we know it will be a collection
        isLocal = isLocal || resourceStorePath.endsWith( RepositoryItemUid.PATH_SEPARATOR );
        return isLocal;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {

        // get the path from request UIR
        String resourceStorePath = getResourceStorePath( request );

        // check do we need local access
        boolean isLocal = isLocal( request, resourceStorePath );

        try
        {
            ResourceStore store = getResourceStore( request );

            ResourceStoreRequest req = getResourceStoreRequest( request, resourceStorePath, isLocal );

            StorageItem item = store.retrieveItem( req );

            return renderItem( context, request, response, variant, item );
        }
        catch ( Exception e )
        {
            handleException( request, response, e );

            return null;
        }
    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        try
        {
            String resourceStorePath = this.getResourceStorePath( request );
            ResourceStoreRequest req = getResourceStoreRequest( request, resourceStorePath, this.isLocal(
                request,
                resourceStorePath ) );

            for ( FileItem fileItem : files )
            {
                getResourceStore( request ).storeItem( req, fileItem.getInputStream(), null );
            }
        }
        catch ( Exception t )
        {
            handleException( request, response, t );
        }
        return null;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {

        // get the path from request UIR
        String resourceStorePath = getResourceStorePath( request );

        // check do we need local access
        boolean isLocal = isLocal( request, resourceStorePath );

        try
        {
            ResourceStore store = getResourceStore( request );

            ResourceStoreRequest req = getResourceStoreRequest( request, resourceStorePath, isLocal );

            store.deleteItem( req );
        }
        catch ( Exception e )
        {
            handleException( request, response, e );
        }
    }

    protected String parsePathFromUri( String parsedPath )
    {

        // get rid of query part
        if ( parsedPath.contains( "?" ) )
        {
            parsedPath = parsedPath.substring( 0, parsedPath.indexOf( '?' ) );
        }

        // get rid of reference part
        if ( parsedPath.contains( "#" ) )
        {
            parsedPath = parsedPath.substring( 0, parsedPath.indexOf( '#' ) );
        }

        if ( StringUtils.isEmpty( parsedPath ) )
        {
            parsedPath = "/";
        }

        return parsedPath;
    }

    /**
     * A strategy to get ResourceStore implementor. To be implemented by subclass.
     * 
     * @return
     * @throws NoSuchRepositoryException
     * @throws NoSuchRepositoryGroupException
     * @throws NoSuchRepositoryRouterException
     */
    protected abstract ResourceStore getResourceStore( Request request )
        throws NoSuchResourceStoreException,
            ResourceException;

    /**
     * Centralized way to create ResourceStoreRequests, since we have to fill in various things in Request context, like
     * authenticated username, etc.
     * 
     * @return
     */
    protected ResourceStoreRequest getResourceStoreRequest( Request request, String resourceStorePath, boolean isLocal )
    {
        ResourceStoreRequest result = new ResourceStoreRequest( resourceStorePath, isLocal );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Created ResourceStore request for " + result.getRequestPath() );
        }

        // honor if-modified-since
        if ( request.getConditions().getModifiedSince() != null )
        {
            result.setIfModifiedSince( request.getConditions().getModifiedSince().getTime() );
        }

        // honor if-none-match
        if ( request.getConditions().getNoneMatch() != null && request.getConditions().getNoneMatch().size() > 0 )
        {
            Tag tag = request.getConditions().getNoneMatch().get( 0 );

            result.setIfNoneMatch( tag.getName() );
        }

        // stuff in the originating remote address
        result.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, request.getClientInfo().getAddress() );

        // stuff in the user id if we have it in request
        if ( request.getChallengeResponse() != null && request.getChallengeResponse().getIdentifier() != null )
        {
            result.getRequestContext().put( AccessManager.REQUEST_USER, request.getChallengeResponse().getIdentifier() );
        }

        // this is HTTPS, get the cert and stuff it too for later
        if ( request.isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            List<?> certs = (List<?>) request.getAttributes().get( "org.restlet.https.clientCertificates" );

            result.getRequestContext().put( AccessManager.REQUEST_CERTIFICATES, certs );
        }

        return result;
    }

    protected Object renderItem( Context context, Request req, Response res, Variant variant, StorageItem item )
        throws IOException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            ResourceException
    {
        Representation result = null;

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            // we have a file
            StorageFileItem file = (StorageFileItem) item;

            if ( req.getConditions().getModifiedSince() != null )
            {
                // this is a conditional GET
                if ( file.getModified() > req.getConditions().getModifiedSince().getTime() )
                {
                    result = new StorageFileItemRepresentation( file );
                }
                else
                {
                    res.setStatus( Status.REDIRECTION_NOT_MODIFIED, "The resource is not modified!" );

                    return null;
                }
            }
            else if ( req.getConditions().getNoneMatch() != null && req.getConditions().getNoneMatch().size() > 0
                && file.getAttributes().containsKey( DigestCalculatingInspector.DIGEST_SHA1_KEY ) )
            {
                Tag tag = req.getConditions().getNoneMatch().get( 0 );

                // this is a conditional get using ETag
                if ( !file.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ).equals( tag.getName() ) )
                {
                    result = new StorageFileItemRepresentation( file );
                }
                else
                {
                    throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED, "Resource is not modified." );
                }
            }
            else
            {
                result = new StorageFileItemRepresentation( file );
            }
        }
        else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            // we have a link, dereference it
            // TODO: we should be able to do HTTP redirects too! (parametrize the dereferencing?)
            try
            {
                return renderItem( context, req, res, variant, getNexus().dereferenceLinkItem( (StorageLinkItem) item ) );
            }
            catch ( Exception e )
            {
                handleException( req, res, e );

                return null;
            }
        }
        else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            String resPath = parsePathFromUri( req.getResourceRef().toString() );

            if ( !resPath.endsWith( "/" ) )
            {
                res.redirectPermanent( resPath + "/" );

                return null;
            }

            // we have a collection
            StorageCollectionItem coll = (StorageCollectionItem) item;

            Collection<StorageItem> children = coll.list();

            ContentListResourceResponse response = new ContentListResourceResponse();

            ContentListResource resource;

            List<String> uniqueNames = new ArrayList<String>( children.size() );

            for ( StorageItem child : children )
            {
                if ( !uniqueNames.contains( child.getName() ) )
                {
                    resource = new ContentListResource();

                    resource.setText( child.getName() );

                    resource.setLeaf( !StorageCollectionItem.class.isAssignableFrom( child.getClass() ) );

                    resource.setResourceURI( createChildReference( req, child.getName() ).toString()
                        + ( resource.isLeaf() ? "" : "/" ) );

                    resource.setRelativePath( child.getPath() + ( resource.isLeaf() ? "" : "/" ) );

                    resource.setLastModified( new Date( child.getModified() ) );

                    resource.setSizeOnDisk( StorageFileItem.class.isAssignableFrom( child.getClass() )
                        ? ( (StorageFileItem) child ).getLength()
                        : -1 );

                    response.addData( resource );

                    uniqueNames.add( child.getName() );
                }
            }

            if ( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
            {
                result = serialize( context, req, variant, response );

                result.setModificationDate( new Date( coll.getModified() ) );
            }
            else
            {
                return response;
            }
        }

        return result;
    }

    protected Representation serialize( Context context, Request req, Variant variant, Object payload )
        throws IOException
    {
        // TEXT_HTML is requested by direct browsing (IE)
        // APPLICATION_XML is requested by direct browsing (FF)
        if ( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            HashMap<String, Object> dataModel = new HashMap<String, Object>();

            dataModel.put( "listItems", sortContentListResource( ( (ContentListResourceResponse) payload ).getData() ) );

            dataModel.put( "request", req );

            dataModel.put( "nexusVersion", getNexus().getSystemStatus().getVersion() );

            dataModel.put( "nexusRoot", req.getRootRef().toString() );

            // Load up the template, and pass in the data
            VelocityRepresentation representation = new VelocityRepresentation(
                context,
                "/templates/repositoryContentHtml.vm",
                dataModel,
                variant.getMediaType() );

            return representation;
        }
        return null;
    }

    private List<ContentListResource> sortContentListResource( Collection<ContentListResource> list )
    {
        List<ContentListResource> result = new ArrayList<ContentListResource>( list );

        Collections.sort( result, new Comparator<ContentListResource>()
        {
            public int compare( ContentListResource o1, ContentListResource o2 )
            {
                if ( !o1.isLeaf() )
                {
                    if ( !o2.isLeaf() )
                    {
                        // 2 directories, do a path compare
                        return o1.getText().compareTo( o2.getText() );
                    }
                    else
                    {
                        // first item is a dir, second is a file, dirs always win
                        return 1;
                    }
                }
                else if ( !o2.isLeaf() )
                {
                    // first item is a file, second is a dir, dirs always win
                    return -1;
                }
                else
                {
                    // 2 files, do a path compare
                    return o1.getText().compareTo( o2.getText() );
                }
            }
        } );

        return result;
    }

    /**
     * ResourceStore iface is pretty "chatty" with Exceptions. This is a centralized place to handle them and convert
     * them to proper HTTP status codes and response.
     * 
     * @param t
     */
    protected void handleException( Request req, Response res, Exception t )
        throws ResourceException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Got exception during processing " + req.getMethod() + " " + req.getResourceRef().toString(),
                t );
        }

        if ( t instanceof ResourceException )
        {
            throw (ResourceException) t;
        }
        else if ( t instanceof IllegalArgumentException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal argument:" + t.getMessage() );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof StorageException )
        {
            getLogger().error( "IO error!", t );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotAvailableException )
        {
            throw new ResourceException( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotListableException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof IllegalRequestException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal request:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof IllegalOperationException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal operation:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof UnsupportedStorageOperationException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof NoSuchRepositoryException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof NoSuchResourceStoreException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof NoSuchRepositoryGroupException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof ItemNotFoundException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof AccessDeniedException )
        {
            // TODO: a big fat problem here!
            // this makes restlet code tied to Servlet code, and we what is happening here is VERY dirty!
            HttpServletRequest servletRequest = ( (ServletCall) ( (HttpRequest) req ).getHttpCall() ).getRequest();

            String scheme = (String) servletRequest.getAttribute( NexusHttpAuthenticationFilter.AUTH_SCHEME_KEY );

            ChallengeScheme challengeScheme = null;

            if ( NexusHttpAuthenticationFilter.FAKE_AUTH_SCHEME.equals( scheme ) )
            {
                challengeScheme = new ChallengeScheme( "HTTP_NXBASIC", "NxBasic", "Fake basic HTTP authentication" );
            }
            else
            {
                challengeScheme = ChallengeScheme.HTTP_BASIC;
            }

            String realm = (String) servletRequest.getAttribute( NexusHttpAuthenticationFilter.AUTH_REALM_KEY );

            res.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );

            res.getChallengeRequests().add( new ChallengeRequest( challengeScheme, realm ) );

            // throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED, "Authenticate to access this resource!" );
        }
        else
        {
            getLogger().error( t.getMessage(), t );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
    }
}
