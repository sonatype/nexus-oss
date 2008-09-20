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

import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
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
    
    public RoleResource nexusToRestModel( CRole role )
    {
        //TODO: ultimately this method will take a parameter which is the nexus object
        //and will convert to the rest object
        RoleResource resource = new RoleResource();
        
        resource.setDescription( role.getDescription() );
        resource.setId( role.getId() );
        resource.setName( role.getName() );
        resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
        resource.setSessionTimeout( role.getSessionTimeout() );
        
        for ( String roleId : ( List<String>) role.getRoles() )
        {
            resource.addRole( roleId );
        }
        
        for ( String privId : ( List<String>) role.getPrivileges() )
        {
            resource.addPrivilege( privId );
        }
        
        return resource;
    }
    
    public CRole restToNexusModel( CRole role, RoleResource resource )
    {
        if ( role == null )
        {
            role = new CRole();
        }
        
        role.setDescription( resource.getDescription() );
        role.setName( resource.getName() );
        role.setSessionTimeout( resource.getSessionTimeout() );
        
        role.getRoles().clear();        
        for ( String roleId : ( List<String> ) resource.getRoles() )
        {
            role.addRole( roleId );
        }
        
        role.getPrivileges().clear();
        for ( String privId : ( List<String> ) resource.getPrivileges() )
        {
            role.addPrivilege( privId );
        }
        
        return role;
    }
}
