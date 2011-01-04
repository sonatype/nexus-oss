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
package org.sonatype.nexus.rest.privileges;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.privileges.AbstractPrivilegePlexusResource;

/**
 * Handles the GET and POST request for the Nexus privileges.
 * 
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "TargetPrivilegeListPlexusResource" )
@Path( TargetPrivilegePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class TargetPrivilegePlexusResource
    extends AbstractPrivilegePlexusResource
{
    public static final String RESOURCE_URI = "/privileges_target";
    
    public TargetPrivilegePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new PrivilegeResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:privileges]" );
    }

    /**
     * Add a new set of target privileges against a repository (or group) in nexus.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = PrivilegeResourceRequest.class, output = PrivilegeListResourceResponse.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        PrivilegeResourceRequest resourceRequest = (PrivilegeResourceRequest) payload;
        PrivilegeListResourceResponse result = null;

        if ( resourceRequest != null )
        {
            result = new PrivilegeListResourceResponse();

            PrivilegeResource resource = resourceRequest.getData();

            // currently we are allowing only of repotarget privs, so enforcing checkfor it
            if ( !TargetPrivilegeDescriptor.TYPE.equals( resource.getType() ) )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getErrorResponse( "type", "Not allowed privilege type!" ) );
            }

            List<String> methods = resource.getMethod();

            if ( methods == null || methods.size() == 0 )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getErrorResponse( "method", "No method(s) supplied, must select at least one method." ) );
            }
            else
            {
                try
                {
                    // Add a new privilege for each method
                    for ( String method : methods )
                    {
                        Privilege priv = new Privilege();

                        priv.setName( resource.getName() != null ? resource.getName() + " - (" + method + ")" : null );
                        priv.setDescription( resource.getDescription() );
                        priv.setType( TargetPrivilegeDescriptor.TYPE );

                        priv.addProperty( ApplicationPrivilegeMethodPropertyDescriptor.ID, method );

                        priv.addProperty( TargetPrivilegeRepositoryTargetPropertyDescriptor.ID, resource
                            .getRepositoryTargetId() );

                        priv.addProperty( TargetPrivilegeRepositoryPropertyDescriptor.ID, resource.getRepositoryId() );

                        priv.addProperty( TargetPrivilegeGroupPropertyDescriptor.ID, resource.getRepositoryGroupId() );

                        priv = getSecuritySystem().getAuthorizationManager( DEFAULT_SOURCE ).addPrivilege( priv );

                        result.addData( this.securityToRestModel( priv, request, true ) );
                    }
                }
                catch ( InvalidConfigurationException e )
                {
                    // build and throw exctption
                    handleInvalidConfigurationException( e );
                }
                catch ( NoSuchAuthorizationManagerException e )
                {
                    // we should not get here
                    this.getLogger().warn( "Could not find the default AuthorizationManager", e );
                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
                }
            }
        }
        return result;
    }

}
