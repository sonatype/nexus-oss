package org.sonatype.nexus.rest;

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class StaticResourceFinder
    extends Finder
{
    private final Context context;

    private final StaticResource resource;

    public StaticResourceFinder( Context context, StaticResource resource )
    {
        this.context = context;

        this.resource = resource;
    }

    public Handler createTarget( Request request, Response response )
    {
        StaticResourceResource resourceResource = new StaticResourceResource( context, request, response, resource );

        return resourceResource;
    }

}
