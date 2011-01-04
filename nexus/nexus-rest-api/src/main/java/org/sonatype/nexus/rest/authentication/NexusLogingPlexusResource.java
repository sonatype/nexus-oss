/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.authentication;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.authentication.AbstractLoginPlexusResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author bdemers
 */
@Component( role = PlexusResource.class, hint = "LoginPlexusResource" )
@Path( AbstractLoginPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class NexusLogingPlexusResource
    extends AbstractLoginPlexusResource
{
    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // this is the ONLY resource using authcNxBasic, as the UI can't receive 401 errors from teh server
        // as the browser login pops up, which is no good in this case
        return new PathProtectionDescriptor( getResourceUri(), "authcNxBasic,perms[nexus:authentication]" );
    }
    
    /**
     * Login to the application, will return a set of permissions available to the specified user.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = AuthenticationLoginResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return super.get( context, request, response, variant );
    }
}
