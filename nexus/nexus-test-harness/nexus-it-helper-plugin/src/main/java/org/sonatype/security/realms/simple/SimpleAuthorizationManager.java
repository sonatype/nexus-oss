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
package org.sonatype.security.realms.simple;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

/**
 * A AuthorizationManager is used if an external Realm wants to use its Group/Roles in Nexus. For example, your realm might
 * already contain a group for all of your developers. Exposing these roles will allow Nexus to map your Realms roles to
 * Nexus roles more easily.
 */
// This class must have a role of 'AuthorizationManager', and the hint, must match the result of getSource() and the hint
// of the corresponding Realm.
@Component( role = AuthorizationManager.class, hint = "Simple", description = "Simple Authorization Manager" )
public class SimpleAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

    public static final String SOURCE = "Simple";

    public String getSource()
    {
        return SOURCE;
    }

    private Set<String> listRoleIds()
    {
        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role-xyz" );
        roleIds.add( "role-abc" );
        roleIds.add( "role-123" );

        return roleIds;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();
        for ( String roleId : this.listRoleIds() )
        {
            roles.add( this.toRole( roleId ) );
        }

        return roles;
    }

    private Role toRole( String roleId )
    {
        Role role = new Role();
        role.setRoleId( roleId );
        role.setSource( this.getSource() );
        role.setName( "Role " + roleId );
        role.setReadOnly( true );

        return role;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return null;
    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        for ( Role role : this.listRoles() )
        {
            if ( role.getRoleId().equals( roleId ) )
            {
                return role;
            }
        }
        throw new NoSuchRoleException( "Role '" + roleId + "' not found." );
    }

    public Set<Privilege> listPrivileges()
    {
        return null;
    }

}
