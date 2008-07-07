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
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceStatusResponse;
import org.sonatype.nexus.rest.model.UserRoleResource;
import org.sonatype.nexus.rest.model.UserStatusResource;

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

        //TODO: Retrieve items from Nexus, currently just hardcoded junk
        UserStatusResource resource = new UserStatusResource();
        resource.setEmail( "someemail@someemail.com" );
        resource.setName( "Real Name" );
        resource.setStatus( "active" );
        resource.setUserId( "realuser" );
        resource.setResourceURI( calculateSubReference( resource.getUserId() ).toString() );
        
        UserRoleResource roleResource = new UserRoleResource();
        roleResource.setRoleId( "roleid" );
        roleResource.setRoleName( "rolename" );
        
        resource.addRole( roleResource );
        
        response.addData( resource );
        
        resource = new UserStatusResource();
        
        resource.setEmail( "someotheremail@someotheremail.com" );
        resource.setName( "Realer Name" );
        resource.setStatus( "disabled" );
        resource.setUserId( "realeruser" );
        resource.setResourceURI( calculateSubReference( resource.getUserId() ).toString() );
        
        roleResource = new UserRoleResource();
        roleResource.setRoleId( "roleid" );
        roleResource.setRoleName( "rolename" );
        
        resource.addRole( roleResource );
        
        response.addData( resource );
        
        resource = new UserStatusResource();
        
        resource.setEmail( "yetanotheremail@yetanotheremail.com" );
        resource.setName( "Realest Name" );
        resource.setStatus( "locked" );
        resource.setUserId( "realestuser" );
        resource.setResourceURI( calculateSubReference( resource.getUserId() ).toString() );
        
        roleResource = new UserRoleResource();
        roleResource.setRoleId( "roleid" );
        roleResource.setRoleName( "rolename" );
        
        resource.addRole( roleResource );
        
        response.addData( resource );
        
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
            
            if ( validateFields( resource, representation ) )
            {
                //TODO: actually store the data here
                
                UserResourceStatusResponse response = new UserResourceStatusResponse();
                
                response.setData( requestToResponseModel( request.getData() ) );
                
                response.getData().setUserId( "newuserid" );
                
                getResponse().setEntity( serialize( representation, response ) );
            }
        }
    }
}
