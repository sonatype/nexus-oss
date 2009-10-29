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

import java.util.HashSet;
import java.util.Set;

public class PlexusUser
    implements Comparable<PlexusUser>
{
    private String userId;
    private String name;
    private String emailAddress;
    private String source;
    private Set<PlexusRole> roles = new HashSet<PlexusRole>();
    
    public String getUserId()
    {
        return userId;
    }
    public void setUserId( String userId )
    {
        this.userId = userId;
    }
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getEmailAddress()
    {
        return emailAddress;
    }
    public void setEmailAddress( String emailAddress )
    {
        this.emailAddress = emailAddress;
    }
    public String getSource()
    {
        return source;
    }
    public void setSource( String source )
    {
        this.source = source;
    }
    public Set<PlexusRole> getRoles()
    {
        return roles;
    }
    public void setRoles( Set<PlexusRole> roles )
    {
        this.roles = roles;
    }
    public void addRole( PlexusRole role )
    {
        this.roles.add( role );
    }
    public void removeRole( PlexusRole role )
    {
        for ( PlexusRole existingRole : this.roles )
        {
            if ( existingRole.equals( role ) )
            {
                this.roles.remove( existingRole );
                break;
            }
        }
    }
    public int compareTo( PlexusUser o )
    {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if ( this == o ) 
            return equal;
        
        if ( o == null )
            return after;
        
        if( getUserId() == null && o.getUserId() != null )
            return before;
        else if( getUserId() != null && o.getUserId() == null )
            return after;
        
        // the userIds are not null
        int result = getUserId().compareTo( o.getUserId() );
        if( result != equal)
            return result;

        if( getSource() == null )
            return before;

        // if we are all the way to this point, the userIds are equal and this.getSource != null, so just return a compareTo on the source
        return getSource().compareTo( o.getSource() );
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
        result = prime * result + ( ( userId == null ) ? 0 : userId.hashCode() );
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
        final PlexusUser other = (PlexusUser) obj;
        if ( source == null )
        {
            if ( other.source != null )
                return false;
        }
        else if ( !source.equals( other.source ) )
            return false;
        if ( userId == null )
        {
            if ( other.userId != null )
                return false;
        }
        else if ( !userId.equals( other.userId ) )
            return false;
        return true;
    }
}
