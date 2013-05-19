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
package org.sonatype.security.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;

/**
 * Default implementation of a User.
 * 
 * @author Brian Demers
 */
public class DefaultUser
    implements User, Comparable<User>
{

    private String userId;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String source;

    private UserStatus status;

    private boolean readOnly;

    private Set<RoleIdentifier> roleIdentifiers = new HashSet<RoleIdentifier>();

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
        String name = this.getFirstName() != null ? this.getFirstName() : "";
        if ( StringUtils.isNotEmpty( this.getLastName() ) )
        {
            name += " " + this.getLastName();
        }
        return name;
    }

    public void setName( String name )
    {
        // deprecated, but attempt to use
        if ( StringUtils.isNotEmpty( name ) )
        {
            String[] nameParts = name.trim().split( " ", 2 );
            this.setFirstName( nameParts[0] );
            if ( nameParts.length > 1 )
            {
                this.setLastName( nameParts[1] );
            }
        }
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( String lastName )
    {
        this.lastName = lastName;
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

    public Set<RoleIdentifier> getRoles()
    {
        return Collections.unmodifiableSet( roleIdentifiers );
    }

    public void addRole( RoleIdentifier roleIdentifier )
    {
        this.roleIdentifiers.add( roleIdentifier );
    }

    public boolean removeRole( RoleIdentifier roleIdentifier )
    {
        return this.roleIdentifiers.remove( roleIdentifier );
    }

    public void addAllRoles( Set<RoleIdentifier> roleIdentifiers )
    {
        this.roleIdentifiers.addAll( roleIdentifiers );
    }

    public void setRoles( Set<RoleIdentifier> roles )
    {
        this.roleIdentifiers = roles;
    }

    public UserStatus getStatus()
    {
        return status;
    }

    public void setStatus( UserStatus status )
    {
        this.status = status;
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
    public String toString()
    {
        return "UserId: " + this.userId + ", Name: " + this.firstName + " " + this.lastName;
    }

    public int compareTo( User o )
    {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if ( this == o )
            return equal;

        if ( o == null )
            return after;

        if ( getUserId() == null && o.getUserId() != null )
            return before;
        else if ( getUserId() != null && o.getUserId() == null )
            return after;

        // the userIds are not null
        int result = getUserId().compareTo( o.getUserId() );
        if ( result != equal )
            return result;

        if ( getSource() == null )
            return before;

        // if we are all the way to this point, the userIds are equal and this.getSource != null, so just return a
        // compareTo on the source
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
        final User other = (User) obj;
        if ( source == null )
        {
            if ( other.getSource() != null )
                return false;
        }
        else if ( !source.equals( other.getSource() ) )
            return false;
        if ( userId == null )
        {
            if ( other.getUserId() != null )
                return false;
        }
        else if ( !userId.equals( other.getUserId() ) )
            return false;
        return true;
    }

}
