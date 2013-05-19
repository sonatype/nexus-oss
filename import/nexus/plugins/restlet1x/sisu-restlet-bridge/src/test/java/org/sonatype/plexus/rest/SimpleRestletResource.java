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
