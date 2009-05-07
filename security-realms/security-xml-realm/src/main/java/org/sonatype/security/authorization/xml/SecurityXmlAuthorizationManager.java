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
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.model.CProperty;
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

    protected Role toRole( SecurityRole secRole )
    {
        Role role = new Role();

        role.setRoleId( secRole.getId() );
        role.setName( secRole.getName() );
        role.setSource( SOURCE );
        role.setDescription( secRole.getDescription() );
        role.setReadOnly( secRole.isReadOnly() );
        role.setSessionTimeout( secRole.getSessionTimeout() );
        role.setPermissions( new HashSet<String>( secRole.getPrivileges() ) );

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
        secRole.setPrivileges( new ArrayList<String>( role.getPermissions() ) );

        return secRole;
    }

    protected SecurityPrivilege toPrivilege( Privilege privilege )
    {
        SecurityPrivilege secPriv = new SecurityPrivilege();
        secPriv.setId( privilege.getId() );
        secPriv.setName( privilege.getName() );
        secPriv.setDescription( privilege.getDescription() );
        secPriv.setReadOnly( privilege.isReadOnly() );
        secPriv.setType( privilege.getType() );

        if ( privilege.getProperties().entrySet() != null )
        {
            for ( Entry<String, String> entry : privilege.getProperties().entrySet() )
            {
                CProperty prop = new CProperty();
                prop.setKey( entry.getKey() );
                prop.setValue( entry.getValue() );
                secPriv.addProperty( prop );
            }
        }

        return secPriv;
    }
    
    protected Privilege toPrivilege( SecurityPrivilege secPriv )
    {
        Privilege privilege = new Privilege();
        privilege.setId( secPriv.getId() );
        privilege.setName( secPriv.getName() );
        privilege.setDescription( secPriv.getDescription() );
        privilege.setReadOnly( secPriv.isReadOnly() );
        privilege.setType( secPriv.getType() );

        if ( secPriv.getProperties() != null )
        {
            for ( CProperty prop : (List<CProperty>) secPriv.getProperties() )
            {
                privilege.addProperty( prop.getKey(), prop.getValue() );
            }
        }

        return privilege;
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
        throws InvalidConfigurationException
    {
        this.configuration.createRole( this.toRole( role ) );
        this.saveConfiguration();

        // TODO: return new role?
        return role;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException,
            InvalidConfigurationException
    {
        this.configuration.updateRole( this.toRole( role ) );
        this.saveConfiguration();
        return role;
    }

    public void deleteRole( String roleId )
        throws NoSuchRoleException
    {
        this.configuration.deleteRole( roleId );
        this.saveConfiguration();
    }

    // //
    // PRIVILEGE CRUDS
    // //

    public Set<Privilege> listPrivileges()
    {
        Set<Privilege> privileges = new HashSet<Privilege>();
        List<SecurityPrivilege> secPrivs = this.configuration.listPrivileges();

        for ( SecurityPrivilege securityPrivilege : secPrivs )
        {
            privileges.add( this.toPrivilege( securityPrivilege ) );
        }

        return privileges;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return this.toPrivilege( this.configuration.readPrivilege( privilegeId ) );
    }

    public Privilege addPrivilege( Privilege privilege ) throws InvalidConfigurationException
    {
       this.configuration.createPrivilege( this.toPrivilege( privilege ) );
       this.saveConfiguration();
       
       return privilege;
    }

    public Privilege updatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException, InvalidConfigurationException
    {
        this.configuration.updatePrivilege( this.toPrivilege( privilege ) );
        this.saveConfiguration();
        
        return privilege;
    }

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        this.configuration.deletePrivilege( privilegeId );
        this.saveConfiguration();
    }
    
    private void saveConfiguration()
    {
        this.configuration.save();
    }

    public boolean supportsWrite()
    {
        return true;
    }


}
