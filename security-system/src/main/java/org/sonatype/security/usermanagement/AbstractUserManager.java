package org.sonatype.security.usermanagement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.authorization.Role;

public abstract class AbstractUserManager
    implements UserManager
{

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
        Set<String> userRoles = new HashSet<String>();
        if ( user.getRoles() != null )
        {
            for ( Role role : user.getRoles() )
            {
                userRoles.add( role.getRoleId() );
            }
        }

        return matchesCriteria( user.getUserId(), user.getSource(), userRoles, criteria );
    }

    protected boolean matchesCriteria( String userId, String userSource, Collection<String> usersRoles,
        UserSearchCriteria criteria )
    {
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !userId.toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }

        if ( criteria.getSource() != null && !criteria.getSource().equals( userSource ) )
        {
            return false;
        }

        if ( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            if ( usersRoles != null )
            {
                userRoles.addAll( usersRoles );
            }

            // check the intersection of the roles
            if ( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty() )
            {
                return false;
            }
        }

        return true;
    }
    
    
    public boolean supportsWrite()
    {
        return false;
    }
    
    public User addUser( User user )
    {
        return null;
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {        
    }

    public Set<Role> getUsersRoles( String userId, String source )
        throws UserNotFoundException
    {
        return null;
    }

    public void setUsersRoles( String userId, Set<Role> roles, String source )
        throws UserNotFoundException
    {        
    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        return null;
    }

}
