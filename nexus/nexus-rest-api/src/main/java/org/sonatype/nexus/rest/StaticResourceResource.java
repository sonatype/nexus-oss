package org.sonatype.nexus.rest;

import java.util.Collections;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class StaticResourceResource
    extends Resource
{
    private final StaticResource resource;

    public StaticResourceResource( Context ctx, Request req, Response rsp, StaticResource resource )
    {
        super( ctx, req, rsp );

        setVariants( Collections.singletonList( new Variant( MediaType.valueOf( resource.getContentType() ) ) ) );

        this.resource = resource;
    }

    public Representation represent( Variant variant )
        throws ResourceException
    {
        return new StaticResourceRepresentation( resource );
    }

}
