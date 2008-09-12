package org.sonatype.plexus.rest.resource;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

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

    Object get( Context context, Request request, Response response )
        throws ResourceException;

    void post( Context context, Request request, Response response, Object payload )
        throws ResourceException;

    void put( Context context, Request request, Response response, Object payload )
        throws ResourceException;

    void delete( Context context, Request request, Response response )
        throws ResourceException;

    void upload( Context context, Request request, Response response, FileItem file )
        throws ResourceException;
}
