/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.rest.roles;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PlexusRoleListResourceResponse;

/**
 *  REST resource for listing security roles.
 * 
 * @author bdemers
 * @see RoleListPlexusResource
 */
@Component(role=PlexusResource.class, hint="PlexusRoleListPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( PlexusRoleListPlexusResource.RESOURCE_URI )
@Deprecated
public class PlexusRoleListPlexusResource
    extends AbstractSecurityPlexusResource
{
    
    public static final String SOURCE_ID_KEY = "sourceId";

    public static final String RESOURCE_URI = "/plexus_roles/{" + SOURCE_ID_KEY + "}";
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_roles/*", "authcBasic,perms[security:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
     * Retrieves the list of security roles.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PlexusRoleListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String source = this.getSourceId( request );

        // get roles for the source
        Set<Role> roles;
        try
        {
            roles = this.getSecuritySystem().listRoles( source );
        }
        catch ( NoSuchAuthorizationManager e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Role Source '" + source
                + "' could not be found." );
        }
        
        PlexusRoleListResourceResponse resourceResponse = new PlexusRoleListResourceResponse();
        for ( Role role : roles )
        {
            resourceResponse.addData( this.securityToRestModel( role ) );
        }

        return resourceResponse;
    }

    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }
}
