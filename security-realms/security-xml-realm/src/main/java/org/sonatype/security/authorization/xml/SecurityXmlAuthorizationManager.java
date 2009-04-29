/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.authorization.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.realms.tools.dao.SecurityRole;

/**
 * RoleLocator that wraps roles from security-xml-realm.
 */
@Component( role = AuthorizationManager.class )
public class SecurityXmlAuthorizationManager
    implements AuthorizationManager
{

    public static final String SOURCE = "default";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public String getSource()
    {
        return SOURCE;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();
        List<SecurityRole> secRoles = this.configuration.listRoles();

        for ( SecurityRole securityRole : secRoles )
        {
            roles.add( this.toRole( securityRole ) );
        }

        return roles;
    }

    protected Role toRole( SecurityRole secRole )
    {

        Role role = new Role();

        role.setRoleId( secRole.getId() );
        role.setName( secRole.getName() );
        role.setSource( SOURCE );

        return role;

    }

    public Set<String> listPermissions()
    {
        Set<String> permissions = new HashSet<String>();
        List<SecurityPrivilege> secPrivs = this.configuration.listPrivileges();

        for ( SecurityPrivilege securityPrivilege : secPrivs )
        {
            // FIXME: use PermissionDescriptors
            permissions.add( securityPrivilege.getId());
        }

        return permissions;
    }

    public Privilege getPrivilege()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role getRole( String roleId )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Privilege> listPrivileges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege addPrivilege( Privilege privilege )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role addRole( Role role )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        
    }

    public void deleteRole( String roleId )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub
        
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege upatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
