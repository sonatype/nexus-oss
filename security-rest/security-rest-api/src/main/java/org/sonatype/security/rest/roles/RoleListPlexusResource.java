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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;

/**
 * REST resource for listing security roles.
 * 
 * @author tstevens
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "RoleListPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( RoleListPlexusResource.RESOURCE_URI )
public class RoleListPlexusResource
    extends AbstractRolePlexusResource
{

    public static final String RESOURCE_URI = "/roles";

    public RoleListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RoleResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:roles]" );
    }

    /**
     * Retrieves the list of security roles.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RoleListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RoleListResourceResponse result = new RoleListResourceResponse();

        try
        {
            for ( Role role : getSecuritySystem().getAuthorizationManager( DEFAULT_SOURCE ).listRoles() )
            {
                RoleResource res = securityToRestModel( role, request, true );

                if ( res != null )
                {
                    result.addData( res );
                }
            }
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().error( "Unable to find AuthorizationManager 'default'", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to find AuthorizationManager 'default'" );
        }

        return result;
    }

    /**
     * Creates a new security role.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = RoleResourceRequest.class, output = RoleResourceResponse.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RoleResourceRequest resourceRequest = (RoleResourceRequest) payload;
        RoleResourceResponse result = null;

        if ( resourceRequest != null )
        {
            RoleResource resource = resourceRequest.getData();

            Role role = restToSecurityModel( null, resource );

            try
            {
                validateRoleContainment( role );

                AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( ROLE_SOURCE );
                role = authzManager.addRole( role );

                result = new RoleResourceResponse();

                resource.setId( role.getRoleId() );

                resource.setUserManaged( true );

                resource.setResourceURI( createChildReference( request, resource.getId() ).toString() );

                result.setData( resource );
            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
            catch ( NoSuchAuthorizationManagerException e )
            {
                this.getLogger().warn( "Could not found AuthorizationManager: " + ROLE_SOURCE, e );
                // we should not ever get here
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Authorization Manager for: "
                    + ROLE_SOURCE + " could not be found." );
            }
        }
        return result;
    }

}
