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
package org.sonatype.nexus.test.utils;

import java.util.List;

import org.sonatype.security.model.CRole;
import org.sonatype.security.rest.model.RoleResource;


public class RoleConverter
{
    

    public static RoleResource toRoleResource( CRole role )
    {
        //TODO: ultimately this method will take a parameter which is the nexus object
        //and will convert to the rest object
        RoleResource resource = new RoleResource();
        
        resource.setDescription( role.getDescription() );
        resource.setId( role.getId() );
        resource.setName( role.getName() );
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
    
    public static CRole toCRole( RoleResource resource )
    {
        CRole role = new CRole();
        
        role.setId( resource.getId()  );
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
