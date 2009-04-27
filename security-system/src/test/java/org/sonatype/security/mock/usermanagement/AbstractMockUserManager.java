package org.sonatype.security.mock.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public abstract class AbstractMockUserManager
    implements UserManager
{

    private Set<User> users = new HashSet<User>();

    public boolean supportsWrite()
    {
        return true;
    }

    public User addUser( User user )
    {
        this.getUsers().add( user );
        return user;
    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        User existingUser = this.getUser( user.getUserId() );

        if ( existingUser == null )
        {
            throw new UserNotFoundException( user.getUserId() );
        }

        return user;
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {
        User existingUser = this.getUser( userId );

        if ( existingUser == null )
        {
            throw new UserNotFoundException( userId );
        }

        this.getUsers().remove( existingUser );

    }

    public User getUser( String userId )
    {
        for ( User user : this.getUsers() )
        {
            if ( user.getUserId().equals( userId ) )
            {
                return user;
            }
        }
        return null;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( User user : this.getUsers() )
        {
            userIds.add( user.getUserId() );
        }

        return userIds;
    }

    public Set<User> listUsers()
    {
        return Collections.unmodifiableSet( this.getUsers() );
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.getUsers(), criteria );
    }

    protected Set<User> getUsers()
    {
        return users;
    }

    protected void setUsers( Set<User> users )
    {
        this.users = users;
    }

    protected Set<User> filterListInMemeory( Set<User> users, UserSearchCriteria criteria )
    {
        HashSet<User> result = new HashSet<User>();

        for ( User user : users )
        {
            if ( userMatchesCriteria( user, criteria ) )
            {
                // add the user if it matches the search criteria
                result.add( user );
            }
        }

        return result;
    }

    protected boolean userMatchesCriteria( User user, UserSearchCriteria criteria )
    {
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !user.getUserId().toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }
        
        if( criteria.getSource() != null && !criteria.getSource().equals( user.getSource() ))
        {
            return false;
        }

        if ( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            if ( user.getRoles() != null )
            {
                for ( Role role : user.getRoles() )
                {
                    userRoles.add( role.getRoleId() );
                }
            }

            // check the intersection of the roles
            if ( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty() )
            {
                return false;
            }
        }

        return true;
    }

    public Set<Role> getUsersRoles( String userId )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setUsersRoles( String userId, Set<Role> roles )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        
    }

    
    
}
