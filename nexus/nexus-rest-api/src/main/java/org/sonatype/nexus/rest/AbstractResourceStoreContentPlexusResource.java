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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;

/**
 * This is an abstract resource handler that uses ResourceStore implementor and
 * publishes those over REST.
 * 
 * @author cstamas
 */
public abstract class AbstractResourceStoreContentPlexusResource extends
        AbstractNexusPlexusResource {

    public static final String IS_LOCAL_PARAMETER = "isLocal";

    
    @Override
    public Object get(Context context, Request request, Response response, Variant variant)
            throws ResourceException {

        // get the path from request UIR
        String resourceStorePath = parsePathFromUri( request.getResourceRef().getRemainingPart() );

        // check do we need local only access
        boolean isLocal = request.getResourceRef().getQueryAsForm().getFirst( IS_LOCAL_PARAMETER ) != null;

        // overriding isLocal is we know it will be a collection
        isLocal = isLocal || resourceStorePath.endsWith( RepositoryItemUid.PATH_SEPARATOR );


        try
        {
            ResourceStore store = getResourceStore( request );

            ResourceStoreRequest req = getResourceStoreRequest( request, resourceStorePath, isLocal );

            StorageItem item = store.retrieveItem( req );

            return renderItem( context, request, response, variant, item );
        }
        catch ( Exception e )
        {
            handleException( e );

            return null;
        }
    }
    
    protected String parsePathFromUri(String parsedPath) {

        // get rid of query part
        if (parsedPath.contains("?")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('?'));
        }

        // get rid of reference part
        if (parsedPath.contains("#")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('#'));
        }

        if (StringUtils.isEmpty(parsedPath)) {
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
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            NoSuchRepositoryRouterException,
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

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Created ResourceStore request for " + result.getRequestPath() );
        }

        result
            .getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, request.getClientInfo().getAddress() );

        if ( request.getChallengeResponse() != null && request.getChallengeResponse().getIdentifier() != null )
        {
            result.getRequestContext().put(
                AccessManager.REQUEST_USER,
                request.getChallengeResponse().getIdentifier() );
        }

        if ( request.isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            // X509Certificate[] certs = (X509Certificate[]) context.getHttpServletRequest().getAttribute(
            // "javax.servlet.request.X509Certificate" );
            // if ( false ) // certs != null )
            // {
            // result.getRequestContext().put( CertificateBasedAccessDecisionVoter.REQUEST_CERTIFICATES, certs );
            // }
        }
        return result;

    }


    protected Object renderItem( Context context, Request req, Response res, Variant variant, StorageItem item )
        throws IOException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
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
                    res.setStatus( Status.REDIRECTION_NOT_MODIFIED );

                    return null;
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
                return renderItem( context, req, res, variant, getNexusInstance(req).dereferenceLinkItem( (StorageLinkItem) item ) );
            }
            catch ( Exception e )
            {
                handleException( e );

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

            if ( MediaType.TEXT_HTML.equals( variant.getMediaType() ) ) {
              result = serialize( context, req, variant, response );
              result.setModificationDate( new Date( coll.getModified() ) );
            }
            else {
                return response;
            }
        }

        return result;
    }


    protected Representation serialize( Context context, Request req, Variant variant, Object payload ) throws IOException
    {
        // TEXT_HTML is requested by direct browsing (IE)
        // APPLICATION_XML is requested by direct browsing (FF)
        if ( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            HashMap<String, Object> dataModel = new HashMap<String, Object>();

            dataModel.put( "listItems", sortContentListResource( ( (ContentListResourceResponse) payload ).getData() ) );

            dataModel.put( "request", req );

            // Load up the template, and pass in the data
            TemplateRepresentation representation = new TemplateRepresentation(
                "/templates/repositoryContentHtml.vm",
                dataModel,
                variant.getMediaType() );

            // Setup the velocity classloader, to find the template properly
            VelocityEngine engine = representation.getEngine();

            engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new RestletLogChute( context ) );

            engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class" );

            engine.setProperty(
                "class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );

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
    protected void handleException( Exception t ) throws ResourceException
    {
        if ( t instanceof IllegalArgumentException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal argument:" + t.getMessage() );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof StorageException )
        {
            getLogger().error( "IO error!", t );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else if ( t instanceof UnsupportedStorageOperationException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, t.getMessage() );
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
        else if ( t instanceof NoSuchRepositoryRouterException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotAvailableException )
        {
            throw new ResourceException( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotListableException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof ItemNotFoundException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof AccessDeniedException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED, "Authenticate to access this resource!" );
        }
        else
        {
            getLogger().error( t.getMessage(), t );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
    }
}
