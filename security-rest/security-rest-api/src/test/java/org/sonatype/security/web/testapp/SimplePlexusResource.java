/**
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
package org.sonatype.security.web.testapp;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A Resource that simple returns "Hello".
 */
@Singleton
@Typed( PlexusResource.class )
@Named( "sample-resource" )
public class SimplePlexusResource
    extends AbstractPlexusResource
{

    public Object getPayloadInstance()
    {
        // do nothing, this is a read only resource.
        return null;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        // any users with the permission 'sample:priv-name:read' can access this resource, NOTE: the 'read' part is
        // because we are doing a GET.
        return new PathProtectionDescriptor( this.getResourceUri(), "authcBasic,perms[sample:priv-name]" );
    }

    public String getResourceUri()
    {
        // we need to say where we are mounting this resource.
        return "/test";
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return "Hello";
    }

}
