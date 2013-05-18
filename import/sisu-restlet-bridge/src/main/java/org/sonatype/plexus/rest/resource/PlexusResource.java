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

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import com.thoughtworks.xstream.XStream;

/**
 * An automatically managed Rest Resource.
 * 
 * @author cstamas
 */
public interface PlexusResource
{
    /**
     * The location to attach this resource to.
     * 
     * @return
     */
    String getResourceUri();

    /**
     * A factory method to create an instance of DTO.
     * 
     * @return
     */
    Object getPayloadInstance();

    /**
     * A factory method to create an instance of DTO per method.
     * 
     * @return
     */
    Object getPayloadInstance( Method method );

    /**
     * A Resource may add some configuration stuff to the XStream, and control the serialization of the payloads it
     * uses.
     * 
     * @param xstream
     */
    void configureXStream( XStream xstream );

    /**
     * A permission prefix to be applied when securing the resource.
     * 
     * @return
     */
    PathProtectionDescriptor getResourceProtection();

    /**
     * Presents a modifiable list of available variants.
     * 
     * @return
     */
    List<Variant> getVariants();

    /**
     * Does resource accepts any call?
     * 
     * @return
     */
    boolean isAvailable();

    /**
     * Does resource accepts GET method?
     * 
     * @return
     */
    boolean isReadable();

    /**
     * Does resource accepts PUT, POST, DELETE methods?
     * 
     * @return
     */
    boolean isModifiable();

    /**
     * If true, Restlet will try to negotiate the "best" content.
     * 
     * @return
     */
    boolean isNegotiateContent();

    /**
     * If true, will redirect POST and PUT to as many upload() method calls, as many files are in request.
     * 
     * @return
     */
    boolean acceptsUpload();

    /**
     * Method invoked on incoming GET request. The method may return: Representation (will be passed unchanged to
     * restlet engine), InputStream (will be wrapped into InputStreamRepresentation), String (will be wrapped into
     * StringRepresentation) and Object. If Object is none of those previously listed, an XStream serialization is
     * applied to it (into variant originally negotiated with client).
     * 
     * @param context - the cross-request context
     * @param request - the request
     * @param response - the response
     * @param variant - the result of the content negotiation (for use by PlexusResources that want's to cruft manually
     *        some Representation).
     * @return Object to be returned to the client. Object may be: InputStream, restlet.org Representation, String or
     *         any object. The "any" object will be serialized by XStream to a proper mediaType if possible.
     * @throws ResourceException
     */
    Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException;

    /**
     * Method invoked on incoming POST request. For return Object, see GET method.
     * 
     * @param context - the cross-request context
     * @param request - the request
     * @param response = the response
     * @param payload - the deserialized payload (if it was possible to deserialize). Otherwise, the Representation is
     *        accessible thru request. If deserialization was not possible it is null.
     * @return
     * @throws ResourceException
     */
    Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException;

    /**
     * Method invoked on incoming PUT request. For return Object, see GET method.
     * 
     * @param context - the cross-request context
     * @param request - the request
     * @param response = the response
     * @param payload - the deserialized payload (if it was possible to deserialize). Otherwise, the Representation is
     *        accessible thru request. If deserialization was not possible it is null.
     * @return
     * @throws ResourceException
     */
    Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException;

    /**
     * Method invoked on incoming DELETE request.
     * 
     * @param context - the cross-request context
     * @param request - the request
     * @param response = the response
     * @throws ResourceException
     */
    void delete( Context context, Request request, Response response )
        throws ResourceException;

    /**
     * "Catch all" method if this method accepts uploads (acceptsUpload() returns true). In this case, the PUT and POST
     * requests will be redirected to this method. For return Object, see GET method.
     * 
     * @param context - the cross-request context
     * @param request - the request
     * @param response = the response
     * @param files
     * @return
     * @throws ResourceException
     */
    Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException;
}
