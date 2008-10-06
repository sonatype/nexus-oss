package org.sonatype.plexus.rest;

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.RestletResource;

/**
 * A finder that holds reference to a PlexusResource (which is a Plexus component) and ties them on incoming request
 * with RestletResource.
 * 
 * @author jvanzyl
 * @author cstamas
 */
public class PlexusResourceFinder
    extends Finder
{
    private PlexusResource plexusResource;

    private Context context;

    public PlexusResourceFinder( Context context, PlexusResource resource )
    {
        this.plexusResource = resource;

        this.context = context;
    }

    public Handler createTarget( Request request, Response response )
    {
        RestletResource restletResource = new RestletResource( context, request, response, plexusResource );

        // init must-have stuff
        restletResource.setContext( context );
        restletResource.setRequest( request );
        restletResource.setResponse( response );

        return restletResource;
    }
}
