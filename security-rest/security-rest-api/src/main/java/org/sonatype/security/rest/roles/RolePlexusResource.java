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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;

/**
 *  REST resource for managing security roles.
 * @author tstevens
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "RolePlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( RolePlexusResource.RESOURCE_URI )
public class RolePlexusResource
    extends AbstractRolePlexusResource
{

    public static final String ROLE_ID_KEY = "roleId";

    public static final String RESOURCE_URI = "/roles/{" + ROLE_ID_KEY + "}";
    
    public RolePlexusResource()
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
        return new PathProtectionDescriptor( "/roles/*", "authcBasic,perms[security:roles]" );
    }

    protected String getRoleId( Request request )
    {
        return getRequestAttribute( request, ROLE_ID_KEY );
    }

    /**
     * Returns the request security role.
     * @param roleId Id of the role.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RoleResourceResponse.class, pathParams = { @PathParam(value = "roleId") }  )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RoleResourceResponse result = new RoleResourceResponse();

        try
        {
            AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( ROLE_SOURCE );
            result.setData( securityToRestModel( authzManager.getRole( getRoleId( request ) ), request, false ) );

        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().warn( "Could not found AuthorizationManager: " + ROLE_SOURCE, e );
            // we should not ever get here
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Authorization Manager for: " + ROLE_SOURCE
                + " could not be found." );
        }
        return result;
    }

    /**
     * Updates and returns a security role.
     * @param roleId Id of the role to be updated.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = RoleResourceRequest.class, output = RoleListResourceResponse.class, pathParams = { @PathParam(value = "roleId") }  )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RoleResourceRequest resourceRequest = (RoleResourceRequest) payload;
        RoleResourceResponse resourceResponse = new RoleResourceResponse();

        if ( resourceRequest != null )
        {
            RoleResource resource = resourceRequest.getData();

            try
            {
                AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( ROLE_SOURCE );
                Role role = restToSecurityModel( authzManager.getRole( resource.getId() ), resource );

                validateRoleContainment( role );

                authzManager.updateRole( role );

                resourceResponse = new RoleResourceResponse();

                resourceResponse.setData( resourceRequest.getData() );

                resourceResponse.getData().setUserManaged( !role.isReadOnly() );

                resourceResponse.getData().setResourceURI(
                    createChildReference( request, resource.getId() ).toString() );

            }
            catch ( NoSuchRoleException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
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
        return resourceResponse;
    }

    /**
     * Removes a security role.
     * @param roleId Id of the role to be removed.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam(value = "roleId") } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( ROLE_SOURCE );
            authzManager.deleteRole( getRoleId( request ) );
        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().warn( "Could not found AuthorizationManager: " + ROLE_SOURCE, e );
            // we should not ever get here
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Authorization Manager for: "
                + ROLE_SOURCE + " could not be found." );
        }
    }

}
