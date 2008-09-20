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
package org.sonatype.nexus.rest.users;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceResponse;

public class UserListResourceHandler
extends AbstractUserResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public UserListResourceHandler( Context context, Request request, Response response )
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
        UserListResourceResponse response = new UserListResourceResponse();
        
        for ( CUser user : getNexusSecurity().listUsers() )
        {
            UserResource res = nexusToRestModel( user );
            
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
        UserResourceRequest request = (UserResourceRequest) deserialize( new UserResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            UserResource resource = request.getData();
            
            CUser user = restToNexusModel( null, resource );
            
            try
            {
                getNexusSecurity().createUser( user );
                
                UserResourceResponse response = new UserResourceResponse();
                
                // Update the status, as that may have changed
                resource.setStatus( user.getStatus() );
                
                resource.setResourceURI( calculateSubReference( resource.getUserId() ).toString() );
                
                response.setData( resource );
                
                getResponse().setEntity( serialize( representation, response ) );
                
                getResponse().setStatus( Status.SUCCESS_CREATED );
            }
            catch ( InvalidConfigurationException e )
            {
                handleInvalidConfigurationException( e, representation );
            }
        }
    }
}
