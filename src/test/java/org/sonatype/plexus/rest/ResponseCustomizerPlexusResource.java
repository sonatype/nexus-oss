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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.RestletResponseCustomizer;

public class ResponseCustomizerPlexusResource
    extends SimplePlexusResource
{

    @Override
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final Object result = super.get( context, request, response, variant );
        return new CustomStringRepresentation( (String) result );
    }

    private static class CustomStringRepresentation
        extends StringRepresentation
        implements RestletResponseCustomizer
    {

        public CustomStringRepresentation( CharSequence text )
        {
            super( text );
        }

        public void customize( final Response response )
        {
            addHttpResponseHeader( response, "X-Custom", "foo" );
        }

    }

}
