package org.sonatype.nexus.rest;

import org.restlet.Context;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.plexus.rest.PlexusResourceFinder;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.RestletResource;

public class NexusPlexusResourceFinder
    extends PlexusResourceFinder
{
    private PlexusResource plexusResource;

    private Context context;
    
    public NexusPlexusResourceFinder( Context context, PlexusResource resource )
    {
        super( context, resource );
        
        this.plexusResource = resource;
        this.context = context;
    }
    
    @Override
    public Handler createTarget( Request request, Response response )
    {
        RestletResource restletResource = new NexusRestletResource( getContext(), request, response, plexusResource );

        // init must-have stuff
        restletResource.setContext( context );
        restletResource.setRequest( request );
        restletResource.setResponse( response );

        return restletResource;
    }
}
