package org.sonatype.plexus.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;

/**
 * A simple testing resource. Will "publish" itself on passed in token URI (/token) and will emit "token" for GETs and
 * respond with HTTP 200 to all other HTTP methods (PUT, POST) only if the token equals to entity passed in.
 * 
 * @author cstamas
 */
public class SimplePlexusResource
    extends AbstractPlexusResource
{
    private String token;

    public SimplePlexusResource()
    {
        super();
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/" + token;
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = new ArrayList<Variant>();

        result.add( new Variant( MediaType.TEXT_PLAIN ) );

        return result;
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return token;
    }

    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        if ( !token.equals( payload.toString() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        return null;
    }

    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        if ( !token.equals( payload.toString() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        return null;
    }

    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        // nothing
    }

    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        return null;
    }

}
