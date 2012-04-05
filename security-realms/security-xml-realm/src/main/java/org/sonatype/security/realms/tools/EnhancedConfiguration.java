/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;

@SuppressWarnings( "serial" )
public class EnhancedConfiguration
    extends Configuration
{
    private final Configuration delegate;

    public EnhancedConfiguration( final Configuration configuration )
    {
        this.delegate = configuration;

        rebuildId2UsersLookupMap();
        rebuildId2RolesLookupMap();
        rebuildId2PrivilegesLookupMap();
        rebuildId2RoleMappingsLookupMap();
    }

    // ==

    public void addPrivilege( CPrivilege cPrivilege )
    {
        delegate.addPrivilege( cPrivilege );

        id2privileges.put( cPrivilege.getId(), cPrivilege );
    }

    public void addRole( CRole cRole )
    {
        delegate.addRole( cRole );

        id2roles.put( cRole.getId(), cRole );
    }

    public void addUser( CUser cUser )
    {
        delegate.addUser( cUser );

        id2users.put( cUser.getId(), cUser );
    }

    public void addUserRoleMapping( CUserRoleMapping cUserRoleMapping )
    {
        delegate.addUserRoleMapping( cUserRoleMapping );

        id2roleMappings.put( getUserRoleMappingKey( cUserRoleMapping.getUserId(), cUserRoleMapping.getSource() ),
            cUserRoleMapping );
    }

    public String getModelEncoding()
    {
        return delegate.getModelEncoding();
    }

    public List<CPrivilege> getPrivileges()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return Collections.unmodifiableList( delegate.getPrivileges() );
    }

    public List<CRole> getRoles()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return Collections.unmodifiableList( delegate.getRoles() );
    }

    public List<CUserRoleMapping> getUserRoleMappings()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return Collections.unmodifiableList( delegate.getUserRoleMappings() );
    }

    public List<CUser> getUsers()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return Collections.unmodifiableList( delegate.getUsers() );
    }

    public String getVersion()
    {
        return delegate.getVersion();
    }

    public void removePrivilege( CPrivilege cPrivilege )
    {
        id2privileges.remove( cPrivilege.getId() );

        delegate.removePrivilege( cPrivilege );
    }

    public void removeRole( CRole cRole )
    {
        id2roles.remove( cRole.getId() );

        delegate.removeRole( cRole );
    }

    public void removeUser( CUser cUser )
    {
        id2users.remove( cUser.getId() );

        delegate.removeUser( cUser );
    }

    public void removeUserRoleMapping( CUserRoleMapping cUserRoleMapping )
    {
        id2roleMappings.remove( getUserRoleMappingKey( cUserRoleMapping.getUserId(), cUserRoleMapping.getSource() ) );

        delegate.removeUserRoleMapping( cUserRoleMapping );
    }

    public void setModelEncoding( String modelEncoding )
    {
        delegate.setModelEncoding( modelEncoding );
    }

    public void setPrivileges( List<CPrivilege> privileges )
    {
        delegate.setPrivileges( privileges );

        rebuildId2PrivilegesLookupMap();
    }

    public void setRoles( List<CRole> roles )
    {
        delegate.setRoles( roles );

        rebuildId2RolesLookupMap();
    }

    public void setUserRoleMappings( List<CUserRoleMapping> userRoleMappings )
    {
        delegate.setUserRoleMappings( userRoleMappings );

        rebuildId2RoleMappingsLookupMap();
    }

    public void setUsers( List<CUser> users )
    {
        delegate.setUsers( users );

        rebuildId2UsersLookupMap();
    }

    public void setVersion( String version )
    {
        delegate.setVersion( version );
    }

    public String toString()
    {
        return super.toString() + " delegating to " + delegate.toString();
    }

    // ==
    // Enhancements

    public CUser getUserById( final String id )
    {
        return id2users.get( id );
    }

    public boolean removeUserById( final String id )
    {
        CUser user = getUserById( id );

        if ( user != null )
        {
            delegate.removeUser( user );
            return id2users.remove( id ) != null;
        }
        else
        {
            return false;
        }
    }

    public CRole getRoleById( final String id )
    {
        return id2roles.get( id );
    }

    public boolean removeRoleById( final String id )
    {
        CRole role = getRoleById( id );

        if ( role != null )
        {
            delegate.removeRole( role );
            return id2roles.remove( id ) != null;
        }
        else
        {
            return false;
        }
    }

    public CPrivilege getPrivilegeById( final String id )
    {
        return id2privileges.get( id );
    }

    public boolean removePrivilegeById( final String id )
    {
        CPrivilege privilege = getPrivilegeById( id );

        if ( privilege != null )
        {
            delegate.removePrivilege( privilege );
            return id2privileges.remove( id ) != null;
        }
        else
        {
            return false;
        }
    }

    public CUserRoleMapping getUserRoleMappingByUserId( final String id, final String source )
    {
        return id2roleMappings.get( getUserRoleMappingKey( id, source ) );
    }

    public boolean removeUserRoleMappingByUserId( final String id, final String source )
    {
        CUserRoleMapping mapping = getUserRoleMappingByUserId( id, source );

        if ( mapping != null )
        {
            delegate.removeUserRoleMapping( mapping );
            return id2roleMappings.remove( getUserRoleMappingKey( id, source ) ) != null;
        }
        else
        {
            return false;
        }
    }

    // ==

    private HashMap<String, CUser> id2users = new HashMap<String, CUser>();

    private HashMap<String, CRole> id2roles = new HashMap<String, CRole>();

    private HashMap<String, CPrivilege> id2privileges = new HashMap<String, CPrivilege>();

    private HashMap<String, CUserRoleMapping> id2roleMappings = new HashMap<String, CUserRoleMapping>();

    protected void rebuildId2UsersLookupMap()
    {
        id2users.clear();

        for ( CUser user : getUsers() )
        {
            id2users.put( user.getId(), user );
        }
    }

    protected void rebuildId2RolesLookupMap()
    {
        id2roles.clear();

        for ( CRole role : getRoles() )
        {
            id2roles.put( role.getId(), role );
        }
    }

    protected void rebuildId2PrivilegesLookupMap()
    {
        id2privileges.clear();

        for ( CPrivilege privilege : getPrivileges() )
        {
            id2privileges.put( privilege.getId(), privilege );
        }
    }

    protected void rebuildId2RoleMappingsLookupMap()
    {
        id2roleMappings.clear();

        for ( CUserRoleMapping user2role : getUserRoleMappings() )
        {
            id2roleMappings.put( getUserRoleMappingKey( user2role.getUserId(), user2role.getSource() ), user2role );
        }
    }

    // ==

    protected String getUserRoleMappingKey( final String userId, final String source )
    {
        return userId.toLowerCase() + "|" + source;
    }
}
