package org.sonatype.plexus.rest;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class SimpleRestletResource
    extends Resource
{
    public SimpleRestletResource( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().add( new Variant( MediaType.TEXT_PLAIN ) );
    }

    public Representation represent( Variant variant )
    {
        String name = getRequest().getResourceRef().getPath();

        if ( name.contains( "/" ) )
        {
            name = name.substring( name.lastIndexOf( "/" ) + 1, name.length() );
        }

        return new StringRepresentation( name );
    }
}
