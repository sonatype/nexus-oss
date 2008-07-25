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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.rest.model.RoleListResourceResponse;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;

public class RoleListResourceHandler
extends AbstractRoleResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RoleListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs/
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Repositories by getting the from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        RoleListResourceResponse response = new RoleListResourceResponse();

        for ( CRole role : getNexusSecurityConfiguration().listRoles() )
        {
            RoleResource res = nexusToRestModel( role );
            
            if ( res != null )
            {
                response.addData( res );
            }
        }
        
        return serialize( variant, response );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation representation )
    {
        RoleResourceRequest request = (RoleResourceRequest) deserialize( new RoleResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            RoleResource resource = request.getData();
            
            CRole role = restToNexusModel( null, resource );
            
            try
            {
                getNexusSecurityConfiguration().createRole( role );
                
                RoleResourceResponse response = new RoleResourceResponse();
                
                resource.setId( role.getId() );
                
                response.setData( resource );
                
                getResponse().setEntity( serialize( representation, response ) );
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e, representation );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
        }
    }
}
