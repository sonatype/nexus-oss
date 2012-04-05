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
package org.sonatype.security.rest.privileges;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.rest.model.PrivilegeStatusResourceResponse;

/**
 * REST resource for managing security privileges.
 * 
 * @author tstevens
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "PrivilegePlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( PrivilegePlexusResource.RESOURCE_URI )
public class PrivilegePlexusResource
    extends AbstractPrivilegePlexusResource
{
    
    public static final String RESOURCE_URI = "/privileges/{" + PRIVILEGE_ID_KEY + "}";

    protected static final String PRIVILEGE_SOURCE = "default";

    public PrivilegePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/privileges/*", "authcBasic,perms[security:privileges]" );
    }

    protected String getPrivilegeId( Request request )
    {
        return getRequestAttribute( request, PRIVILEGE_ID_KEY );
    }

    /**
     * Retrieves the details of a security privilege.
     * @param privilegeId The Id of the privilege.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PrivilegeStatusResourceResponse.class, pathParams = { @PathParam(value = "privilegeId") } )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeStatusResourceResponse result = new PrivilegeStatusResourceResponse();

        Privilege priv = null;

        try
        {
            AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( PRIVILEGE_SOURCE );
            priv = authzManager.getPrivilege( getPrivilegeId( request ) );
        }
        catch ( NoSuchPrivilegeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Privilege could not be found." );
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().warn( "Could not found AuthorizationManager: " + PRIVILEGE_SOURCE, e );
            // we should not ever get here
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Authorization Manager for: "
                + PRIVILEGE_SOURCE + " could not be found." );
        }

        result.setData( securityToRestModel( priv, request, false ) );

        return result;
    }

    /**
     * Removes a security privilege.
     * 
     * @param privilegeId The Id of the privilege to be removed.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam(value = "privilegeId") } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        Privilege priv;

        try
        {
            AuthorizationManager authzManager = getSecuritySystem().getAuthorizationManager( PRIVILEGE_SOURCE );

            priv = authzManager.getPrivilege( getPrivilegeId( request ) );

            if ( priv.getType().equals( ApplicationPrivilegeDescriptor.TYPE ) )
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot delete an application type privilege" );
            }
            else
            {
                authzManager.deletePrivilege( getPrivilegeId( request ) );
            }
        }
        catch ( NoSuchPrivilegeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            this.getLogger().warn( "Could not found AuthorizationManager: "+ PRIVILEGE_SOURCE, e );
            // we should not ever get here
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Authorization Manager for: "
                + PRIVILEGE_SOURCE + " could not be found." );
        }
    }

}
