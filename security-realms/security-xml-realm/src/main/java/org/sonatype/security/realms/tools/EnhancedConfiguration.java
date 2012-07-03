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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

@SuppressWarnings( "serial" )
public class EnhancedConfiguration
    extends Configuration
{
    private final Configuration delegate;

    public EnhancedConfiguration( final Configuration configuration )
    {
        this.delegate = Preconditions.checkNotNull( configuration );
        rebuildId2UsersLookupMap();
        rebuildId2RolesLookupMap();
        rebuildId2PrivilegesLookupMap();
        rebuildId2RoleMappingsLookupMap();
    }

    // ==

    @Override
    public void addPrivilege( final CPrivilege cPrivilege )
    {
        final CPrivilege cp = cPrivilege.clone();
        delegate.addPrivilege( cp );
        id2privileges.put( cp.getId(), cp );
    }

    @Override
    public void addRole( final CRole cRole )
    {
        final CRole cr = cRole.clone();
        delegate.addRole( cr );
        id2roles.put( cr.getId(), cr );
    }

    @Override
    public void addUser( final CUser cUser )
    {
        final CUser cu = cUser.clone();
        delegate.addUser( cu );
        id2users.put( cu.getId(), cu );
    }

    @Override
    public void addUserRoleMapping( final CUserRoleMapping cUserRoleMapping )
    {
        final CUserRoleMapping curm = cUserRoleMapping.clone();
        delegate.addUserRoleMapping( curm );
        id2roleMappings.put( getUserRoleMappingKey( curm.getUserId(), curm.getSource() ), curm );
    }

    @Override
    public String getModelEncoding()
    {
        return delegate.getModelEncoding();
    }

    @Override
    public List<CPrivilege> getPrivileges()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return ImmutableList.copyOf( Collections2.transform( delegate.getPrivileges(),
            new Function<CPrivilege, CPrivilege>()
            {
                @Override
                public CPrivilege apply( @Nullable CPrivilege input )
                {
                    return input.clone();
                }
            } ) );
    }

    @Override
    public List<CRole> getRoles()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return ImmutableList.copyOf( Collections2.transform( delegate.getRoles(), new Function<CRole, CRole>()
        {
            @Override
            public CRole apply( @Nullable CRole input )
            {
                return input.clone();
            }
        } ) );
    }

    @Override
    public List<CUserRoleMapping> getUserRoleMappings()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return ImmutableList.copyOf( Collections2.transform( delegate.getUserRoleMappings(),
            new Function<CUserRoleMapping, CUserRoleMapping>()
            {
                @Override
                public CUserRoleMapping apply( @Nullable CUserRoleMapping input )
                {
                    return input.clone();
                }
            } ) );
    }

    @Override
    public List<CUser> getUsers()
    {
        // we are intentionally breaking code that will try to _modify_ the list
        // as the old config manager was before we fixed it
        return ImmutableList.copyOf( Collections2.transform( delegate.getUsers(), new Function<CUser, CUser>()
        {
            @Override
            public CUser apply( @Nullable CUser input )
            {
                return input.clone();
            }
        } ) );
    }

    @Override
    public String getVersion()
    {
        return delegate.getVersion();
    }

    @Override
    public void removePrivilege( final CPrivilege cPrivilege )
    {
        id2privileges.remove( cPrivilege.getId() );
        delegate.removePrivilege( cPrivilege );
    }

    @Override
    public void removeRole( final CRole cRole )
    {
        id2roles.remove( cRole.getId() );
        delegate.removeRole( cRole );
    }

    @Override
    public void removeUser( final CUser cUser )
    {
        id2users.remove( cUser.getId() );
        delegate.removeUser( cUser );
    }

    @Override
    public void removeUserRoleMapping( final CUserRoleMapping cUserRoleMapping )
    {
        id2roleMappings.remove( getUserRoleMappingKey( cUserRoleMapping.getUserId(), cUserRoleMapping.getSource() ) );
        delegate.removeUserRoleMapping( cUserRoleMapping );
    }

    @Override
    public void setModelEncoding( final String modelEncoding )
    {
        delegate.setModelEncoding( modelEncoding );
    }

    @Override
    public void setPrivileges( final List<CPrivilege> privileges )
    {
        delegate.setPrivileges( privileges );
        rebuildId2PrivilegesLookupMap();
    }

    @Override
    public void setRoles( final List<CRole> roles )
    {
        delegate.setRoles( roles );
        rebuildId2RolesLookupMap();
    }

    @Override
    public void setUserRoleMappings( final List<CUserRoleMapping> userRoleMappings )
    {
        delegate.setUserRoleMappings( userRoleMappings );
        rebuildId2RoleMappingsLookupMap();
    }

    @Override
    public void setUsers( final List<CUser> users )
    {
        delegate.setUsers( users );
        rebuildId2UsersLookupMap();
    }

    @Override
    public void setVersion( final String version )
    {
        delegate.setVersion( version );
    }

    @Override
    public String toString()
    {
        return super.toString() + " delegating to " + delegate.toString();
    }

    // ==
    // Enhancements

    public CUser getUserById( final String id )
    {
        return getUserById( id, true );
    }

    protected CUser getUserById( final String id, final boolean clone )
    {
        final CUser user = id2users.get( id );
        if ( user != null )
        {
            return clone ? user.clone() : user;
        }
        else
        {
            return null;
        }
    }

    public boolean removeUserById( final String id )
    {
        final CUser user = getUserById( id, false );
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
        return getRoleById( id, true );
    }

    protected CRole getRoleById( final String id, final boolean clone )
    {
        final CRole role = id2roles.get( id );
        if ( role != null )
        {
            return clone ? role.clone() : role;
        }
        else
        {
            return null;
        }
    }

    public boolean removeRoleById( final String id )
    {
        final CRole role = getRoleById( id, false );
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
        return getPrivilegeById( id, true );
    }

    protected CPrivilege getPrivilegeById( final String id, final boolean clone )
    {
        final CPrivilege privilege = id2privileges.get( id );
        if ( privilege != null )
        {
            return clone ? privilege.clone() : privilege;
        }
        else
        {
            return null;
        }
    }

    public boolean removePrivilegeById( final String id )
    {
        final CPrivilege privilege = getPrivilegeById( id, false );
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
        return getUserRoleMappingByUserId( id, source, true );
    }

    protected CUserRoleMapping getUserRoleMappingByUserId( final String id, final String source, final boolean clone )
    {
        final CUserRoleMapping mapping = id2roleMappings.get( getUserRoleMappingKey( id, source ) );
        if ( mapping != null )
        {
            return clone ? mapping.clone() : mapping;
        }
        else
        {
            return null;
        }
    }

    public boolean removeUserRoleMappingByUserId( final String id, final String source )
    {
        final CUserRoleMapping mapping = getUserRoleMappingByUserId( id, source, false );
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

    private final ConcurrentHashMap<String, CUser> id2users = new ConcurrentHashMap<String, CUser>();

    private final ConcurrentHashMap<String, CRole> id2roles = new ConcurrentHashMap<String, CRole>();

    private final ConcurrentHashMap<String, CPrivilege> id2privileges = new ConcurrentHashMap<String, CPrivilege>();

    private final ConcurrentHashMap<String, CUserRoleMapping> id2roleMappings =
        new ConcurrentHashMap<String, CUserRoleMapping>();

    protected void rebuildId2UsersLookupMap()
    {
        id2users.clear();
        for ( CUser user : delegate.getUsers() )
        {
            id2users.put( user.getId(), user );
        }
    }

    protected void rebuildId2RolesLookupMap()
    {
        id2roles.clear();
        for ( CRole role : delegate.getRoles() )
        {
            id2roles.put( role.getId(), role );
        }
    }

    protected void rebuildId2PrivilegesLookupMap()
    {
        id2privileges.clear();
        for ( CPrivilege privilege : delegate.getPrivileges() )
        {
            id2privileges.put( privilege.getId(), privilege );
        }
    }

    protected void rebuildId2RoleMappingsLookupMap()
    {
        id2roleMappings.clear();
        for ( CUserRoleMapping user2role : delegate.getUserRoleMappings() )
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
