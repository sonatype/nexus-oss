package org.sonatype.security.usermanagement;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.security.authorization.Role;

public class DefaultUser
    implements User, Comparable<User>
{

    private String userId;

    private String name;

    private String emailAddress;

    private String source;

    private Set<Role> roles = new HashSet<Role>();

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

    public Set<Role> getRoles()
    {
        return roles;
    }

    public void addRole( Role role )
    {
        this.roles.add( role );
    }

    public void setRoles( Set<Role> roles )
    {
        this.roles = roles;
    }

    @Override
    public String toString()
    {
        return "UserId: " + this.userId + ", Name: " + this.name;
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
