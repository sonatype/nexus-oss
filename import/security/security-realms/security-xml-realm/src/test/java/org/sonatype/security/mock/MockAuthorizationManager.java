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
package org.sonatype.security.mock;

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
@Named( "Mock" )
@Typed( AuthorizationManager.class )
public class MockAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

    public String getSource()
    {
        return "Mock";
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();

        roles.add( new Role( "mockrole1", "MockRole1", "Mock Role1", "Mock", true, null, null ) );
        roles.add( new Role( "mockrole2", "MockRole2", "Mock Role2", "Mock", true, null, null ) );
        roles.add( new Role( "mockrole3", "MockRole3", "Mock Role3", "Mock", true, null, null ) );

        return roles;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        for ( Role role : this.listRoles() )
        {
            if ( roleId.equals( role.getRoleId() ) )
            {
                return role;
            }
        }
        throw new NoSuchRoleException( "Role: " + roleId + " could not be found." );
    }

    public Set<Privilege> listPrivileges()
    {
        return new HashSet<Privilege>();
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        throw new NoSuchPrivilegeException( "Privilege: " + privilegeId + " could not be found." );
    }

}
