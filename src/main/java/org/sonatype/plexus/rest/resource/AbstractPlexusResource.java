package org.sonatype.plexus.rest.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public abstract class AbstractPlexusResource
    extends AbstractLogEnabled
    implements PlexusResource
{
    private boolean available = true;

    private boolean readable = true;

    private boolean modifiable = false;

    private boolean negotiateContent = true;

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

    public abstract String getPermissionPrefix();

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

}
