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
package org.sonatype.plexus.rest.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * The delegating resource.
 *
 * @author Jason van Zyl
 * @author cstamas
 */
public class RestletResource
    extends Resource
{
    private PlexusResource delegate;

    public RestletResource( Context context, Request request, Response response, PlexusResource delegate )
    {
        super( context, request, response );

        this.delegate = delegate;

        // set variants
        getVariants().clear();
        getVariants().addAll( delegate.getVariants() );

        // mimic the constructor
        setAvailable( delegate.isAvailable() );
        setReadable( delegate.isReadable() );
        setModifiable( delegate.isModifiable() );
        setNegotiateContent( delegate.isNegotiateContent() );
    }

    private String getModificationDateKey( boolean parent )
    {
        if ( parent )
        {
            return getRequest().getResourceRef().getParentRef().getPath() + "#modified";
        }
        else
        {
            return getRequest().getResourceRef().getPath() + "#modified";
        }
    }

    protected Date getModificationDate()
    {
        Date result = (Date) getContext().getAttributes().get( getModificationDateKey( false ) );

        if ( result == null )
        {
            // get parent's date
            result = (Date) getContext().getAttributes().get( getModificationDateKey( true ) );

            if ( result == null )
            {
                // get app date
                PlexusRestletApplicationBridge application = (PlexusRestletApplicationBridge) getApplication();

                result = application.getCreatedOn();
            }

            getContext().getAttributes().put( getModificationDateKey( false ), result );
        }

        return result;
    }

    protected void updateModificationDate( boolean parent )
    {
        getContext().getAttributes().put( getModificationDateKey( parent ), new Date() );
    }

    /**
     * For file uploads we are using commons-fileupload integration with restlet.org. We are storing one FileItemFactory
     * instance in context. This method simply encapsulates gettting it from Resource context.
     *
     * @return
     */
    protected FileItemFactory getFileItemFactory()
    {
        return (FileItemFactory) getContext().getAttributes().get( PlexusRestletApplicationBridge.FILEITEM_FACTORY );
    }

    protected XStreamRepresentation createRepresentation( Variant variant )
        throws ResourceException
    {
        XStreamRepresentation representation = null;

        try
        {
            // check is this variant a supported one, to avoid calling getText() on potentially huge representations
            if ( MediaType.APPLICATION_JSON.equals( variant.getMediaType(), true )
                || MediaType.APPLICATION_XML.equals( variant.getMediaType(), true )
                || MediaType.TEXT_HTML.equals( variant.getMediaType(), true ) )
            {
                String text = ( variant instanceof Representation ) ? ( (Representation) variant ).getText() : "";

                if ( MediaType.APPLICATION_JSON.equals( variant.getMediaType(), true )
                    || MediaType.TEXT_HTML.equals( variant.getMediaType(), true ) )
                {
                    representation =
                        new XStreamRepresentation(
                                                   (XStream) getContext().getAttributes().get(
                                                                                               PlexusRestletApplicationBridge.JSON_XSTREAM ),
                                                   text, variant.getMediaType() );
                }
                else if ( MediaType.APPLICATION_XML.equals( variant.getMediaType(), true ) )
                {
                    representation =
                        new XStreamRepresentation(
                                                   (XStream) getContext().getAttributes().get(
                                                                                               PlexusRestletApplicationBridge.XML_XSTREAM ),
                                                   text, variant.getMediaType() );
                }

                representation.setModificationDate( getModificationDate() );

                return representation;
            }
            else
            {
                return null;
            }
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Cannot get the representation!", e );
        }
    }

    protected Representation serialize( Variant variant, Object payload )
        throws ResourceException
    {
        if ( payload == null )
        {
            return null;
        }

        XStreamRepresentation result = createRepresentation( variant );

        if ( result == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_ACCEPTABLE, "The requested mediaType='"
                + variant.getMediaType() + "' is unsupported!" );
        }

        result.setPayload( payload );

        return result;
    }

    protected Object deserialize( Object root )
        throws ResourceException
    {

        Object result = null;

        if ( root != null )
        {

            if ( String.class.isAssignableFrom( root.getClass() ) )
            {
                try
                {
                    result = getRequest().getEntity().getText();
                }
                catch ( IOException e )
                {
                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Cannot get the representation!", e );
                }
            }

            XStreamRepresentation representation = createRepresentation( getRequest().getEntity() );

            if ( representation != null )
            {
                try
                {
                    result = representation.getPayload( root );
                }
                catch ( XStreamException e )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                                 "Invalid XML, unable to parse using XStream", e );
                }
            }
        }
        return result;
    }

    protected Representation doRepresent( Object payload, Variant variant )
        throws ResourceException
    {
        if ( Representation.class.isAssignableFrom( payload.getClass() ) )
        {
            // representation
            return (Representation) payload;
        }
        else if ( InputStream.class.isAssignableFrom( payload.getClass() ) )
        {
            // inputStream
            return new InputStreamRepresentation( variant.getMediaType(), (InputStream) payload );
        }
        else if ( String.class.isAssignableFrom( payload.getClass() ) )
        {
            // inputStream
            return new StringRepresentation( (String) payload, variant.getMediaType() );
        }
        else
        {
            // object, make it a representation
            return serialize( variant, payload );
        }
    }

    @Override
    public Representation represent( Variant variant )
        throws ResourceException
    {
        Object result = delegate.get( getContext(), getRequest(), getResponse(), variant );

        return ( result != null ) ? doRepresent( result, variant ) : null;
    }

    @Override
    public void acceptRepresentation( Representation representation )
        throws ResourceException
    {
        if ( delegate.acceptsUpload() )
        {
            upload( representation );
        }
        else
        {
            Object payload = deserialize( delegate.getPayloadInstance() );

            Object result = null;

            try
            {
                result = delegate.post( getContext(), getRequest(), getResponse(), payload );

                // This is a post, so set the status correctly
                // but only if the status was not changed to be something else, like a 202
                if ( getResponse().getStatus() == Status.SUCCESS_OK )
                {
                    getResponse().setStatus( Status.SUCCESS_CREATED );
                }
            }
            catch ( PlexusResourceException e )
            {
                // set the status
                getResponse().setStatus( e.getStatus() );
                // try to get the responseObject
                result = e.getResultObject();
            }

            if ( result != null )
            {
                getResponse().setEntity( doRepresent( result, representation ) );
            }
        }

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );
        }
    }

    @Override
    public void storeRepresentation( Representation representation )
        throws ResourceException
    {
        if ( delegate.acceptsUpload() )
        {
            upload( representation );
        }
        else
        {
            Object payload = deserialize( delegate.getPayloadInstance() );

            Object result = null;
            try
            {
                result = delegate.put( getContext(), getRequest(), getResponse(), payload );

            }
            catch ( PlexusResourceException e )
            {
                // set the status
                getResponse().setStatus( e.getStatus() );
                // try to get the responseObject
                result = e.getResultObject();
            }

            if ( result != null )
            {
                getResponse().setEntity( doRepresent( result, representation ) );
            }
        }

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );

            updateModificationDate( true );
        }
    }

    @Override
    public void removeRepresentations()
        throws ResourceException
    {
        delegate.delete( getContext(), getRequest(), getResponse() );

        // if we have an Entity set, then return a 200 (default)
        // if not return a 204
        if ( getResponse().getStatus() == Status.SUCCESS_OK && !getResponse().isEntityAvailable() )
        {
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );

            updateModificationDate( true );
        }
    }

    public void upload( Representation representation )
        throws ResourceException
    {
        Object result = null;

        List<FileItem> files = null;

        try
        {
            RestletFileUpload uploadRequest = new RestletFileUpload( getFileItemFactory() );

            files = uploadRequest.parseRepresentation( representation );

            result = delegate.upload( getContext(), getRequest(), getResponse(), files );
        }
        catch ( FileUploadException e )
        {
            // try to take simply the body as stream
            String name = getRequest().getResourceRef().getPath();

            if ( name.contains( "/" ) )
            {
                name = name.substring( name.lastIndexOf( "/" ) + 1, name.length() );
            }

            FileItem file = new FakeFileItem( name, representation );

            files = new ArrayList<FileItem>();

            files.add( file );

            result = delegate.upload( getContext(), getRequest(), getResponse(), files );
        }

        // only if the status was not changed to be something else, like a 202
        if ( getResponse().getStatus() == Status.SUCCESS_OK )
        {
            getResponse().setStatus( Status.SUCCESS_CREATED );
        }

        if ( result != null )
        {
            // TODO: representation cannot be returned as multipart! (representation above is possibly multipart)
            getResponse().setEntity( doRepresent( result, getPreferredVariant() ) );
        }
    }

    // ==

    private class FakeFileItem
        implements FileItem
    {
        private static final long serialVersionUID = 414885488690939983L;

        private final String name;

        private final Representation representation;

        public FakeFileItem( String name, Representation representation )
        {
            this.name = name;

            this.representation = representation;
        }

        public String getContentType()
        {
            return representation.getMediaType().getName();
        }

        public String getName()
        {
            return name;
        }

        public String getFieldName()
        {
            return getName();
        }

        public InputStream getInputStream()
            throws IOException
        {
            return representation.getStream();
        }

        // == ignored methods

        public void delete()
        {
            // TODO Auto-generated method stub
        }

        public byte[] get()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public OutputStream getOutputStream()
            throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        public long getSize()
        {
            return 0;
        }

        public String getString()
        {
            return null;
        }

        public String getString( String encoding )
            throws UnsupportedEncodingException
        {
            return null;
        }

        public boolean isFormField()
        {
            return false;
        }

        public boolean isInMemory()
        {
            return false;
        }

        public void setFieldName( String name )
        {
        }

        public void setFormField( boolean state )
        {
        }

        public void write( File file )
            throws Exception
        {
        }

    }

}
