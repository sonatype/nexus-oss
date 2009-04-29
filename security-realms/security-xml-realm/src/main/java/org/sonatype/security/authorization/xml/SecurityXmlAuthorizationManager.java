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

import java.util.ArrayList;
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
import org.sonatype.security.realms.tools.InvalidConfigurationException;
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

    protected Role toRole( SecurityRole secRole )
    {
        Role role = new Role();

        role.setRoleId( secRole.getId() );
        role.setName( secRole.getName() );
        role.setSource( SOURCE );
        role.setDescription( secRole.getDescription() );
        role.setReadOnly( secRole.isReadOnly() );
        role.setSessionTimeout( secRole.getSessionTimeout() );
        role.setPermissions(  new HashSet<String>(secRole.getPrivileges() ) );

        return role;
    }

    protected SecurityRole toRole( Role role )
    {
        SecurityRole secRole = new SecurityRole();
        
        secRole.setId( role.getRoleId() );
        secRole.setName( role.getName() );
        secRole.setDescription( role.getDescription() );
        secRole.setReadOnly( role.isReadOnly() );
        secRole.setSessionTimeout( role.getSessionTimeout() );
        secRole.setPrivileges(  new ArrayList<String>(role.getPermissions() ) );

        return secRole;
    }

    // //
    // ROLE CRUDS
    // //

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

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        return this.toRole( this.configuration.readRole( roleId ) );
    }

    public Role addRole( Role role )
    {
        try
        {
            this.configuration.createRole( this.toRole( role ) );
        }
        catch ( InvalidConfigurationException e )
        {
            // FIXME Auto-generated catch block
            e.printStackTrace();
        }

        return role;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteRole( String roleId )
        throws NoSuchRoleException
    {
        // TODO Auto-generated method stub

    }

    // //
    // PRIVILEGE CRUDS
    // //

    public Set<String> listPermissions()
    {
        Set<String> permissions = new HashSet<String>();
        List<SecurityPrivilege> secPrivs = this.configuration.listPrivileges();

        for ( SecurityPrivilege securityPrivilege : secPrivs )
        {
            // FIXME: use PermissionDescriptors
            permissions.add( securityPrivilege.getId() );
        }

        return permissions;
    }

    public Set<Privilege> listPrivileges()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Privilege addPrivilege( Privilege privilege )
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

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        // TODO Auto-generated method stub

    }

}
