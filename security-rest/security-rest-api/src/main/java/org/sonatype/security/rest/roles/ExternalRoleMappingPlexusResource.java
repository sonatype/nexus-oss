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
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager;

/**
 * REST resource for searching a give role in a realm.
 * 
 * @author velo
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "ExternalRoleMappingPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( ExternalRoleMappingPlexusResource.RESOURCE_URI )
public class ExternalRoleMappingPlexusResource
    extends AbstractRolePlexusResource
{

    public static final String SOURCE_ID_KEY = "sourceId";

    public static final String ROLE_ID_KEY = "roleId";
    
    public static final String RESOURCE_URI = "/external_role_map/{" + SOURCE_ID_KEY + "}/{" + ROLE_ID_KEY + "}";
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/external_role_map/*/*", "authcBasic,perms[security:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
    * Retrieves the list of external role mappings.
    * 
     * @param sourceId The Id of the source.  A source specifies where the users/roles came from, 
     * for example the source Id of 'LDAP' identifies the users/roles as coming from an LDAP source.
    */
    @Override
    @GET
    @ResourceMethodSignature( output = ExternalRoleMappingResourceResponse.class, pathParams = {
        @PathParam( value = ExternalRoleMappingPlexusResource.SOURCE_ID_KEY ),
        @PathParam( value = ExternalRoleMappingPlexusResource.ROLE_ID_KEY ) } )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String sourceId = this.getSourceId( request );
        String roleId = this.getRoleId( request );

        AuthorizationManager source;
        try
        {
            source = getSecuritySystem().getAuthorizationManager( sourceId );
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid source id '" + sourceId + "'", e );
        }

        final Role role;
        try
        {
            role = source.getRole( roleId );
        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Invalid role id '" + roleId + "' on realm '"
                + sourceId + "'", e );
        }

        Role defaultRole;
        try
        {
            defaultRole = getSecuritySystem().getAuthorizationManager( SecurityXmlUserManager.SOURCE ).getRole( roleId );
        }
        catch ( NoSuchRoleException e )
        {
            defaultRole = null;
        }
        catch ( NoSuchAuthorizationManagerException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to load 'default' realm", e );
        }

        ExternalRoleMappingResourceResponse result = new ExternalRoleMappingResourceResponse();

        ExternalRoleMappingResource resource = new ExternalRoleMappingResource();
        result.setData( resource );
        resource.setDefaultRole( this.securityToRestModel( defaultRole ) );
        resource.addMappedRole( this.securityToRestModel( role ) );

        return result;
    }

    protected String getSourceId( Request request )
    {
        return getRequestAttribute( request, SOURCE_ID_KEY );
    }

    protected String getRoleId( Request request )
    {
        return getRequestAttribute( request, ROLE_ID_KEY );
    }
}
