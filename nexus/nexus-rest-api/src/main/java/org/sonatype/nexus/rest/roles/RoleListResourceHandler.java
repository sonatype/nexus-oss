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
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.RoleContainedPrivilegeResource;
import org.sonatype.nexus.rest.model.RoleContainedRoleResource;
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

        //TODO: Retrieve items from Nexus, currently just hardcoded junk
        RoleResource resource = new RoleResource();
        resource.setDescription( "Some role description" );
        resource.setId( "roleid" );
        resource.setName( "rolename" );
        resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
        resource.setSessionTimeout( 60 );

        RoleContainedRoleResource roleResource = new RoleContainedRoleResource();
        roleResource.setId( "roleid2" );
        roleResource.setName( "rolename2" );
        
        resource.addRole( roleResource );
        
        RoleContainedPrivilegeResource privResource = new RoleContainedPrivilegeResource();
        privResource.setId( "privid" );
        privResource.setName( "privname" );
        
        resource.addPrivilege( privResource );
        
        response.addData( resource );
        
        resource = new RoleResource();
        resource.setDescription( "Some role description" );
        resource.setId( "roleid2" );
        resource.setName( "rolename2" );
        resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
        resource.setSessionTimeout( 60 );
        
        privResource = new RoleContainedPrivilegeResource();
        privResource.setId( "privid" );
        privResource.setName( "privname" );
        
        resource.addPrivilege( privResource );
        
        response.addData( resource );
        
        resource = new RoleResource();
        resource.setDescription( "Some role description" );
        resource.setId( "roleid3" );
        resource.setName( "rolename3" );
        resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
        resource.setSessionTimeout( 60 );
        
        privResource = new RoleContainedPrivilegeResource();
        privResource.setId( "privid" );
        privResource.setName( "privname" );
        
        resource.addPrivilege( privResource );
        
        privResource = new RoleContainedPrivilegeResource();
        privResource.setId( "privid2" );
        privResource.setName( "privname2" );
        
        resource.addPrivilege( privResource );
        
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
        RoleResourceRequest request = (RoleResourceRequest) deserialize( new RoleResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            RoleResource resource = request.getData();
            
            if ( validateFields( resource, representation ) )
            {
                //TODO: actually store the data here
                
                RoleResourceResponse response = new RoleResourceResponse();
                
                response.setData( request.getData() );
                
                response.getData().setId( "newroleid" );
                
                getResponse().setEntity( serialize( representation, response ) );
            }
        }
    }
}
