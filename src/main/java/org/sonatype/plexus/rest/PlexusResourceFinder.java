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
