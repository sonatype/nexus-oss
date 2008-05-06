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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessDecisionVoter;
import org.sonatype.nexus.proxy.access.CertificateBasedAccessDecisionVoter;
import org.sonatype.nexus.proxy.access.IpAddressAccessDecisionVoter;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.security.User;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;

/**
 * This is an abstract resource handler that uses ResourceStore implementor and publishes those over REST.
 * 
 * @author cstamas
 */
public abstract class AbstractResourceStoreContentResource
    extends AbstractNexusResourceHandler
{
    private String resourceStorePath;

    /**
     * The default Resource constructor. It is also calculating the path from undelrying resource store. It is actually
     * the "remaining" part from the matched Resource basename. It simply makes the path "absolute" by prepending a
     * slash to them, it is required by ResourceStores.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractResourceStoreContentResource( Context context, Request request, Response response )
    {
        super( context, request, response );

        // overriding the "default" variant to text_html
        getVariants().add( 0, new Variant( MediaType.TEXT_HTML ) );

        resourceStorePath = parsePathFromUri( getRequest().getResourceRef().getRemainingPart() );
    }

    protected String parsePathFromUri( String uri )
    {
        String parsedPath = uri;

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
     * Setter.
     * 
     * @param resourceStorePath
     */
    public void setResourceStorePath( String resourceStorePath )
    {
        this.resourceStorePath = resourceStorePath;
    }

    /**
     * Getter.
     * 
     * @return
     */
    protected String getResourceStorePath()
    {
        return resourceStorePath;
    }

    /**
     * A strategy to get ResourceStore implementor. To be implemented by subclass.
     * 
     * @return
     * @throws NoSuchRepositoryException
     * @throws NoSuchRepositoryGroupException
     * @throws NoSuchRepositoryRouterException
     */
    protected abstract ResourceStore getResourceStore()
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            NoSuchRepositoryRouterException;

    /**
     * Centralized way to create ResourceStoreRequests, since we have to fill in various things in Request context, like
     * authenticated username, etc.
     * 
     * @param isLocal
     * @return
     */
    protected ResourceStoreRequest getResourceStoreRequest( boolean isLocal )
    {
        ResourceStoreRequest result = new ResourceStoreRequest( getResourceStorePath(), isLocal );

        if ( getLogger().isLoggable( Level.FINE ) )
        {
            getLogger().log( Level.FINE, "Created ResourceStore request for " + result.getRequestPath() );
        }

        result.getRequestContext().put(
            IpAddressAccessDecisionVoter.REQUEST_REMOTE_ADDRESS,
            getRequest().getClientInfo().getAddress() );

        if ( getRequest().getAttributes().containsKey( NexusAuthenticationGuard.REST_USER_KEY ) )
        {
            User user = (User) getRequest().getAttributes().get( NexusAuthenticationGuard.REST_USER_KEY );

            result.getRequestContext().put( AccessDecisionVoter.REQUEST_USER, user );
        }

        if ( getRequest().isConfidential() )
        {
            result.getRequestContext().put( CertificateBasedAccessDecisionVoter.REQUEST_SECURE, Boolean.TRUE );

            // X509Certificate[] certs = (X509Certificate[]) context.getHttpServletRequest().getAttribute(
            // "javax.servlet.request.X509Certificate" );
            // if ( false ) // certs != null )
            // {
            // result.getRequestContext().put( CertificateBasedAccessDecisionVoter.REQUEST_CERTIFICATES, certs );
            // }
        }
        return result;

    }

    protected Representation renderItem( Variant variant, StorageItem item )
        throws IOException
    {
        Representation result = null;

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            // we have a file
            StorageFileItem file = (StorageFileItem) item;

            if ( getRequest().getConditions().getModifiedSince() != null )
            {
                // this is a conditional GET
                if ( file.getModified() > getRequest().getConditions().getModifiedSince().getTime() )
                {
                    result = new InputStreamRepresentation( MediaType.valueOf( file.getMimeType() ), file
                        .getInputStream() );
                }
                else
                {
                    getResponse().setStatus( Status.REDIRECTION_NOT_MODIFIED );

                    return null;
                }
            }
            else
            {
                result = new InputStreamRepresentation( MediaType.valueOf( file.getMimeType() ), file.getInputStream() );
            }

            result.setModificationDate( new Date( file.getModified() ) );

            result.setSize( file.getLength() );
        }
        else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            // we have a link, dereference it
            // TODO: we should be able to do HTTP redirects too! (parametrize the dereferencing?)
            try
            {
                return renderItem( variant, getNexus().dereferenceLinkItem( item ) );
            }
            catch ( Throwable e )
            {
                handleException( e );

                return null;
            }
        }
        else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            String resPath = parsePathFromUri( getRequest().getResourceRef().toString() );

            if ( !resPath.endsWith( "/" ) )
            {
                getResponse().redirectPermanent( resPath + "/" );

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

                    resource.setResourceUri( calculateSubReference( child.getName() ).getPath()
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

            result = serialize( variant, response );

            result.setModificationDate( new Date( coll.getModified() ) );
        }

        return result;
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

    protected Representation serialize( Variant variant, Object payload )
    {
        // TEXT_HTML is requested by direct browsing (IE)
        // APPLICATION_XML is requested by direct browsing (FF)
        if ( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            HashMap<String, Object> dataModel = new HashMap<String, Object>();

            dataModel.put( "listItems", sortContentListResource( ( (ContentListResourceResponse) payload ).getData() ) );

            dataModel.put( "request", getRequest() );

            // Load up the template, and pass in the data
            TemplateRepresentation representation = new TemplateRepresentation(
                "/templates/repositoryContentHtml.vm",
                dataModel,
                variant.getMediaType() );

            // Setup the velocity classloader, to find the template properly
            VelocityEngine engine = representation.getEngine();

            engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class" );

            engine.setProperty(
                "class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );

            return representation;
        }
        else
        {
            return super.serialize( variant, payload );
        }
    }

    /**
     * This resource allows HTTP GET.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Handler for gettting presentation invoked on incoming GET request. It simply creates a non-local
     * ResourceStoreRequest, gets the ResourceStore and executes "retrieveItem" method against it. Deending on the
     * returned StorageItem type, it serves then the file content, a link or collection/directory listing.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        try
        {
            ResourceStore store = getResourceStore();

            ResourceStoreRequest req = getResourceStoreRequest( false );

            StorageItem item = store.retrieveItem( req );

            return renderItem( variant, item );
        }
        catch ( Throwable e )
        {
            handleException( e );

            return null;
        }
    }

    /**
     * This resource allows HTTP HEAD.
     * 
     * @return
     */
    public boolean allowHead()
    {
        return true;
    }

    /**
     * The HEAD handler.
     */
    public void handleHead()
    {
        Representation result = null;

        try
        {
            ResourceStore store = getResourceStore();

            ResourceStoreRequest req = getResourceStoreRequest( false );

            StorageItem item = store.retrieveItem( req );

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                // we have a file
                StorageFileItem file = (StorageFileItem) item;

                result = serialize( getPreferredVariant(), file );
            }
            else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
            {
                // we have a link
                StorageLinkItem link = (StorageLinkItem) item;

                result = serialize( getPreferredVariant(), link );
            }
            else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
            {
                // we have a collection
                StorageCollectionItem coll = (StorageCollectionItem) item;

                result = serialize( getPreferredVariant(), coll );
            }

            getResponse().setEntity( result );
        }
        catch ( Throwable e )
        {
            handleException( e );
        }
    }

    /**
     * This resource allows HTTP PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * The PUT handler. With PUT, we should accept a file upload actually. This method gets the uploaded file stream and
     * invokes a "storeItem" method against ResourceStore.
     */
    public void put( Representation entity )
    {
        try
        {
            InputStream body = null;

            try
            {
                // try is it upload. if yes, take the 1st file in set
                RestletFileUpload file = new RestletFileUpload( getFileItemFactory() );

                List<FileItem> files = file.parseRepresentation( entity );

                body = files.get( 0 ).getInputStream();
            }
            catch ( FileUploadException e )
            {
                // try to take simply the body as stream
                body = entity.getStream();
            }

            ResourceStoreRequest req = getResourceStoreRequest( true );

            getResourceStore().storeItem( req, body, null );
        }
        catch ( Throwable t )
        {
            handleException( t );
        }
    }

    /**
     * This resource allows HTTP POST.
     */
    public boolean allowPost()
    {
        return true;
    }

    /**
     * The POST behaviour is equal to PUT behaviour.
     */
    public void post( Representation entity )
    {
        put( entity );
    }

    /**
     * This resource allows HTTP DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * The handler for DELETE. It simply gets the ResourceStore and invokes "deleteItem" against it using the incoming
     * path.
     */
    public void delete()
    {
        try
        {
            ResourceStore store = getResourceStore();

            ResourceStoreRequest req = getResourceStoreRequest( true );

            store.deleteItem( req );
        }
        catch ( Throwable e )
        {
            handleException( e );
        }
    }

    /**
     * ResourceStore iface is pretty "chatty" with Exceptions. This is a centralized place to handle them and convert
     * them to proper HTTP status codes and response.
     * 
     * @param t
     */
    protected void handleException( Throwable t )
    {
        if ( t instanceof IllegalArgumentException )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );

            getLogger().log( Level.SEVERE, "Illegal argument!", t );
        }
        else if ( t instanceof StorageException )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL, t.getMessage() );

            getLogger().log( Level.SEVERE, "IO error!", t );
        }
        else if ( t instanceof NoSuchRepositoryException )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof NoSuchRepositoryGroupException )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof NoSuchRepositoryRouterException )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotAvailableException )
        {
            getResponse().setStatus( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, t.getMessage() );
        }
        else if ( t instanceof ItemNotFoundException )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof AccessDeniedException )
        {
            if ( isRequestAnonymous() )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_UNAUTHORIZED, "Authenticate to access this resource!" );
            }
            else
            {
                getResponse().setStatus( Status.CLIENT_ERROR_FORBIDDEN, "Access to resource is forbidden!" );
            }
        }
        else
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL, t.getMessage() );

            getLogger().log( Level.SEVERE, t.getMessage(), t );
        }
    }
}
