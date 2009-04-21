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
package org.sonatype.jsecurity.locators.users;

import org.codehaus.plexus.util.StringUtils;

public class PlexusRole
    implements Comparable<PlexusRole>
{
    private String roleId;

    private String name;

    private String source;

    public PlexusRole()
    {

    }

    public PlexusRole( String roleId, String name, String source )
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

    public int compareTo( PlexusRole o )
    {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if ( this == o ) 
            return equal;
        
        if ( o == null )
            return after;
        
        if( getRoleId() == null && o.getRoleId() != null )
            return before;
        else if( getRoleId() != null && o.getRoleId() == null )
            return after;
        
        // the roleIds are not null
        int result = getRoleId().compareTo( o.getRoleId() );
        if( result != equal)
            return result;

        if( getSource() == null )
            return before;

        // if we are all the way to this point, the RoleIds are equal and this.getSource != null, so just return a compareTo on the source
        return getSource().compareTo( o.getSource() );
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        final PlexusRole other = (PlexusRole) obj;
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
