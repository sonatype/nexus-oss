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
package org.sonatype.security.rest.roles;

import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PlexusRoleListResourceResponse;

/**
 * REST resource for listing security roles.
 * 
 * @author bdemers
 * @see RoleListPlexusResource
 */
@Singleton
@Typed( PlexusResource.class )
@Named( "PlexusRoleListPlexusResource" )
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
     * 
     * @param sourceId The Id of the source. A source specifies where the users/roles came from, for example the source
     *            Id of 'LDAP' identifies the users/roles as coming from an LDAP source.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PlexusRoleListResourceResponse.class, pathParams = { @PathParam( "sourceId" ) } )
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
        catch ( NoSuchAuthorizationManagerException e )
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
        return getRequestAttribute( request, SOURCE_ID_KEY );
    }
}
