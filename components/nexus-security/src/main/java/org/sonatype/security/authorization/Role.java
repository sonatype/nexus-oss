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
package org.sonatype.security.authorization;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple bean that represents a security Role.
 * 
 * @author Brian Demers
 */
public class Role
    implements Comparable<Role>
{
    private String roleId;

    private String name;

    private String description;

    private String source;

    private boolean readOnly;

    private Set<String> roles = new HashSet<String>();

    private Set<String> privileges = new HashSet<String>();

    public Role()
    {

    }

    public Role( String roleId, String name, String description, String source, boolean readOnly, Set<String> roles,
                 Set<String> privileges )
    {
        this.roleId = roleId;
        this.name = name;
        this.description = description;
        this.source = source;
        this.readOnly = readOnly;
        this.roles = roles;
        this.privileges = privileges;
    }

    public String getRoleId()
    {
        return roleId;
    }

    public void setRoleId( String roleId )
    {
        this.roleId = roleId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void addRole( String role )
    {
        this.roles.add( role );
    }

    public void setRoles( Set<String> roles )
    {
        this.roles = roles;
    }

    public Set<String> getPrivileges()
    {
        return privileges;
    }

    public void addPrivilege( String privilege )
    {
        this.privileges.add( privilege );
    }

    public void setPrivileges( Set<String> privilege )
    {
        this.privileges = privilege;
    }

    public int compareTo( Role o )
    {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if ( this == o )
        {
            return equal;
        }

        if ( o == null )
        {
            return after;
        }

        if ( getRoleId() == null && o.getRoleId() != null )
        {
            return before;
        }
        else if ( getRoleId() != null && o.getRoleId() == null )
        {
            return after;
        }

        // the roleIds are not null
        int result = getRoleId().compareTo( o.getRoleId() );
        if ( result != equal )
        {
            return result;
        }

        if ( getSource() == null )
        {
            return before;
        }

        // if we are all the way to this point, the RoleIds are equal and this.getSource != null, so just return a
        // compareTo on the source
        return getSource().compareTo( o.getSource() );
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( description == null ) ? 0 : description.hashCode() );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( privileges == null ) ? 0 : privileges.hashCode() );
        result = prime * result + ( readOnly ? 1231 : 1237 );
        result = prime * result + ( ( roleId == null ) ? 0 : roleId.hashCode() );
        result = prime * result + ( ( roles == null ) ? 0 : roles.hashCode() );
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        Role other = (Role) obj;
        if ( description == null )
        {
            if ( other.description != null )
            {
                return false;
            }
        }
        else if ( !description.equals( other.description ) )
        {
            return false;
        }
        if ( name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !name.equals( other.name ) )
        {
            return false;
        }
        if ( privileges == null )
        {
            if ( other.privileges != null )
            {
                return false;
            }
        }
        else if ( !privileges.equals( other.privileges ) )
        {
            return false;
        }
        if ( readOnly != other.readOnly )
        {
            return false;
        }
        if ( roleId == null )
        {
            if ( other.roleId != null )
            {
                return false;
            }
        }
        else if ( !roleId.equals( other.roleId ) )
        {
            return false;
        }
        if ( roles == null )
        {
            if ( other.roles != null )
            {
                return false;
            }
        }
        else if ( !roles.equals( other.roles ) )
        {
            return false;
        }
        if ( source == null )
        {
            if ( other.source != null )
            {
                return false;
            }
        }
        else if ( !source.equals( other.source ) )
        {
            return false;
        }
        return true;
    }

}
