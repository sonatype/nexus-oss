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
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.RoleContainedPrivilegeResource;
import org.sonatype.nexus.rest.model.RoleContainedRoleResource;
import org.sonatype.nexus.rest.model.RoleResource;

public class AbstractRoleResourceHandler
extends AbstractNexusResourceHandler
{
    public static final String ROLE_ID_KEY = "roleId";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractRoleResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }
    
    public boolean validateFields( RoleResource resource, Representation representation )
    {
        //TODO: Need to verify that there are no circular loops of role inclusion
        return true;
    }
    
    public RoleResource nexusToRestModel()
    {
        //TODO: ultimately this method will take a parameter which is the nexus object
        //and will convert to the rest object
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
        
        return resource;
    }
}
