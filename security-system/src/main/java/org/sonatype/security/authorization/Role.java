package org.sonatype.security.authorization;

import java.util.HashSet;
import java.util.Set;

public class Role
{
    private String roleId;

    private String name;

    private String source;

    private Set<String> permissions = new HashSet<String>();

    public Role()
    {

    }

    public Role( String roleId, String name, String source )
    {
        this.roleId = roleId;
        this.name = name;
        this.source = source;
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

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void addPermission( String permission )
    {
        this.permissions.add( permission );
    }

    public void setPermissions( Set<String> permissions )
    {
        this.permissions = permissions;
    }

    public int compareTo( Role o )
    {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if ( this == o )
            return equal;

        if ( o == null )
            return after;

        if ( getRoleId() == null && o.getRoleId() != null )
            return before;
        else if ( getRoleId() != null && o.getRoleId() == null )
            return after;

        // the roleIds are not null
        int result = getRoleId().compareTo( o.getRoleId() );
        if ( result != equal )
            return result;

        if ( getSource() == null )
            return before;

        // if we are all the way to this point, the RoleIds are equal and this.getSource != null, so just return a
        // compareTo on the source
        return getSource().compareTo( o.getSource() );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( permissions == null ) ? 0 : permissions.hashCode() );
        result = prime * result + ( ( roleId == null ) ? 0 : roleId.hashCode() );
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Role other = (Role) obj;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        if ( permissions == null )
        {
            if ( other.permissions != null )
                return false;
        }
        else if ( !permissions.equals( other.permissions ) )
            return false;
        if ( roleId == null )
        {
            if ( other.roleId != null )
                return false;
        }
        else if ( !roleId.equals( other.roleId ) )
            return false;
        if ( source == null )
        {
            if ( other.source != null )
                return false;
        }
        else if ( !source.equals( other.source ) )
            return false;
        return true;
    }

}
