/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.roles;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;

public class RoleResourceHandler
    extends AbstractRoleResourceHandler
{
    private String roleId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RoleResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.roleId = getRequest().getAttributes().get( ROLE_ID_KEY ).toString();
    }

    protected String getRoleId()
    {
        return this.roleId;
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Role resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        RoleResourceResponse response = new RoleResourceResponse();
        
        try
        {
            response.setData( nexusToRestModel( getNexusSecurity().readRole( getRoleId() ) ) );
            
            return serialize( variant, response );
        }
        catch ( NoSuchRoleException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

            return null;
        }
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a user.
     */
    public void put( Representation representation )
    {
        RoleResourceRequest request = (RoleResourceRequest) deserialize( new RoleResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            RoleResource resource = request.getData();
            
            try
            {
                CRole role = restToNexusModel( getNexusSecurity().readRole( resource.getId() ), resource );
                
                getNexusSecurity().updateRole( role );
                
                RoleResourceResponse response = new RoleResourceResponse();
                
                response.setData( request.getData() );
                
                response.getData().setResourceURI( calculateSubReference( resource.getId() ).toString() );
                
                getResponse().setEntity( serialize( representation, response ) );
            }
            catch ( NoSuchRoleException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            }
            catch ( InvalidConfigurationException e )
            {
                handleInvalidConfigurationException( e, representation );
            }
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a role.
     */
    public void delete()
    {
        try
        {            
            getNexusSecurity().deleteRole( getRoleId() );
            
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( NoSuchRoleException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
