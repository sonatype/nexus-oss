package org.sonatype.plexus.rest.resource;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.restlet.Context;
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

    public abstract String getResourceUri();

    public abstract List<Variant> getVariants();

    public abstract Object getPayloadInstance();

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

    public boolean acceptsUpload()
    {
        // since this property will not change, it is needed to be overrided
        return false;
    }

    public Object get( Context context, Request request, Response response )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public void post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public void put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
    }

    public void upload( Context context, Request request, Response response, FileItem file )
        throws ResourceException
    {
        throw new ResourceException( Status.SERVER_ERROR_NOT_IMPLEMENTED );
    }

}
