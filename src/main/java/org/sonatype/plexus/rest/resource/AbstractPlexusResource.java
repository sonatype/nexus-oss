/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPlexusResource
    implements PlexusResource
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean available = true;

    private boolean readable = true;

    private boolean modifiable = false;

    private boolean negotiateContent = true;

    protected Logger getLogger() {
        return logger;
    }

    // GETTER/SETTERS, will be unlikely overridden

    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable( boolean available )
    {
        this.available = available;
    }

    public boolean isReadable()
    {
        return readable;
    }

    public void setReadable( boolean readable )
    {
        this.readable = readable;
    }

    public boolean isModifiable()
    {
        return modifiable;
    }

    public void setModifiable( boolean modifiable )
    {
        this.modifiable = modifiable;
    }

    public boolean isNegotiateContent()
    {
        return negotiateContent;
    }

    public void setNegotiateContent( boolean negotiateContent )
    {
        this.negotiateContent = negotiateContent;
    }

    // to be implemented subclasses

    public abstract String getResourceUri();

    public abstract PathProtectionDescriptor getResourceProtection();

    public abstract Object getPayloadInstance();

    // to be overridden by subclasses if needed

    public List<Variant> getVariants()
    {
        ArrayList<Variant> result = new ArrayList<Variant>();

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        result.add( new Variant( MediaType.APPLICATION_JSON ) );

        return result;
    }

    public boolean acceptsUpload()
    {
        // since this property will not change during the lifetime of a resource, it is needed to be overrided
        return false;
    }

    public void configureXStream( XStream xstream )
    {
        // a dummy implementation to be overridden if needed
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        throw new ResourceException( Status.SERVER_ERROR_NOT_IMPLEMENTED );
    }

    public Object getPayloadInstance( org.restlet.data.Method method )
    {
        return getPayloadInstance();
    }

    /**
     * Adds an HTTP header to restlet response.
     *
     * @param response Restlet response to add the HTTP header to
     * @param name     HTTP header name
     * @param value    HTTP header value
     */
    public static void addHttpResponseHeader( final Response response, final String name, final String value )
    {
        Form responseHeaders = (Form) response.getAttributes().get( "org.restlet.http.headers" );

        if ( responseHeaders == null )
        {
            responseHeaders = new Form();
            response.getAttributes().put( "org.restlet.http.headers", responseHeaders );
        }

        responseHeaders.add( name, value );
    }

}
