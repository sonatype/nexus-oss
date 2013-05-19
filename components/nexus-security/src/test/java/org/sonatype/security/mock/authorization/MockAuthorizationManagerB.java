/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.mock.authorization;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

@Singleton
@Typed( AuthorizationManager.class )
@Named( "sourceB" )
public class MockAuthorizationManagerB
    extends AbstractReadOnlyAuthorizationManager
{

    public String getSource()
    {
        return "sourceB";
    }

    public Set<String> listPermissions()
    {
        Set<String> permissions = new HashSet<String>();

        permissions.add( "from-role:read" );
        permissions.add( "from-role:delete" );

        return permissions;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();

        Role role1 = new Role();
        role1.setSource( this.getSource() );
        role1.setName( "Role 1" );
        role1.setRoleId( "test-role1" );
        role1.addPrivilege( "from-role1:read" );
        role1.addPrivilege( "from-role1:delete" );

        Role role2 = new Role();
        role2.setSource( this.getSource() );
        role2.setName( "Role 2" );
        role2.setRoleId( "test-role2" );
        role2.addPrivilege( "from-role2:read" );
        role2.addPrivilege( "from-role2:delete" );

        roles.add( role1 );
        roles.add( role2 );

        return roles;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return null;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        return null;
    }

    public Set<Privilege> listPrivileges()
    {
        return null;
    }

}
