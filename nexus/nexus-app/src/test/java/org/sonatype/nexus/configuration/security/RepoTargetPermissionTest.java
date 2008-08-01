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
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;

import java.util.Set;

public class RepoTargetPermissionTest extends AbstractRealmTest
{
    public void testAnyMethodOrRepositoryPermission()
    {
        Permission assignedPermission = createAssignedTargetPermission( "maven", null, null );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermission );
    }

    public void testAnyRepositoryPermission()
    {
        Permission assignedPermission = createAssignedTargetPermission( "maven", null, "CREATE" );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermission );
    }

    public void testAnyMethodInTargetPermission()
    {
        Permission assignedPermission = createAssignedTargetPermission( "maven", "central", null );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermission );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermission );
    }

    public void testSingleMethodInTargetPermission()
    {
        Permission assignedPermission = createAssignedTargetPermission( "maven", "central", "CREATE" );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermission );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermission );
    }

    public void testAnyMethodInGroupPermission()
    {
        Set<Permission> assignedPermissions = createAssignedGroupPermissions( "maven", "myGroup", null );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:CREATE" ), assignedPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:UPDATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermissions );
    }

    public void testSingleMethodInGroupPermission()
    {
        Set<Permission> assignedPermissions = createAssignedGroupPermissions( "maven", "myGroup", "CREATE" );
        assertImplied( new WildcardPermission( "nexus:target:maven:central:CREATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:central:UPDATE" ), assignedPermissions );
        assertImplied( new WildcardPermission( "nexus:target:maven:myRepository:CREATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:myRepository:UPDATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:CREATE" ), assignedPermissions );
        assertNotImplied( new WildcardPermission( "nexus:target:maven:codehaus:UPDATE" ), assignedPermissions );
    }

    private Permission createAssignedTargetPermission( String repositoryTargetId,
                                                               String repositoryId,
                                                               String method )
    {
        CRepoTargetPrivilege targetPrivilege = new CRepoTargetPrivilege();
        targetPrivilege.setRepositoryTargetId( repositoryTargetId );
        targetPrivilege.setRepositoryId( repositoryId );
        targetPrivilege.setMethod( method );

        Set<Permission> permissions = realm.createPermissions( targetPrivilege );

        // we should have gotten a single WildcardPermission
        assertEquals( 1, permissions.size() );
        Permission permission = permissions.iterator().next();
        assertNotNull( "permission is null", permission );
        assertTrue( "permission should be an instance of WildcardPermission",
            permission instanceof WildcardPermission );

        WildcardPermission assignedPermission = (WildcardPermission) permission;
        return assignedPermission;
    }

    private Set<Permission> createAssignedGroupPermissions( String repositoryTargetId, String groupId, String method )
    {
        CRepoTargetPrivilege targetPrivilege = new CRepoTargetPrivilege();
        targetPrivilege.setRepositoryTargetId( repositoryTargetId );
        targetPrivilege.setGroupId( groupId );
        targetPrivilege.setMethod( method );

        Set<Permission> permissions = realm.createPermissions( targetPrivilege );
        assertFalse( permissions.isEmpty() );
        return permissions;
    }

}
