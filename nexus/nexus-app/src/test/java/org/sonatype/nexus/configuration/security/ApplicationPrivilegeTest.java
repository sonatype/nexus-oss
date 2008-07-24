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
package org.sonatype.nexus.configuration.security;

import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;

public class ApplicationPrivilegeTest extends AbstractRealmTest
{
    public void testAnyPathOrMethodPermission()
    {
        Permission assignedPermission = createAssignedApplicationPermission( null, null );
        assertImplied( new WildcardPermission( "nexus:user:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:user:UPDATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:repository:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:repository:UPDATE" ), assignedPermission );
    }

    public void testAnyMethodForPathPermission()
    {
        Permission assignedPermission = createAssignedApplicationPermission( "nexus:user", null );
        assertImplied( new WildcardPermission( "nexus:user:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:user:UPDATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:repository:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:repository:UPDATE" ), assignedPermission );
    }

    public void testSingleMethodForPathPermission()
    {
        Permission assignedPermission = createAssignedApplicationPermission( "nexus:user", "CREATE" );
        assertImplied( new WildcardPermission( "nexus:user:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:user:UPDATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:repository:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:repository:UPDATE" ), assignedPermission );
    }

    private Permission createAssignedApplicationPermission( String path, String method )
    {
        CApplicationPrivilege applicationPrivilege = new CApplicationPrivilege();
        applicationPrivilege.setPath( path );
        applicationPrivilege.setMethod( method );

        Permission permission = realm.createPermission( applicationPrivilege );

        // we should have gotten a single WildcardPermission
        assertNotNull( "permission is null", permission );
        assertTrue( "permission should be an instance of WildcardPermission",
            permission instanceof WildcardPermission );

        return permission;
    }
}