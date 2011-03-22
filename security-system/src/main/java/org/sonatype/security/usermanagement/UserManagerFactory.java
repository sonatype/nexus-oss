package org.sonatype.security.usermanagement;

import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Typed( value = UserManagerFactory.class )
@Named( "default" )
public class UserManagerFactory
{
    @Inject
    Map<String, UserManager> userManagers;

    @Inject
    Logger logger;

    public User getUser( String userId, String source )
        throws UserNotFoundException, NoSuchUserManagerException
    {
        // first get the user
        // this is the UserManager that owns the user
        UserManager userManager = getUserManager( source );
        User user = userManager.getUser( userId );

        if ( user == null )
        {
            throw new UserNotFoundException( userId );
        }

        // add roles from other user managers
        this.addOtherRolesToUser( user );

        return user;
    }

    public Map<String, UserManager> getUserManagers()
    {
        return userManagers;
    }

    public UserManager getUserManager( String sourceId )
        throws NoSuchUserManagerException
    {
        if ( !userManagers.containsKey( sourceId ) )
        {
            throw new NoSuchUserManagerException( "UserManager with source: '" + sourceId + "' could not be found." );
        }

        return userManagers.get( sourceId );
    }

    private void addOtherRolesToUser( User user )
    {
        // then save the users Roles
        for ( UserManager tmpUserManager : userManagers.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only have roles
            if ( !tmpUserManager.getSource().equals( user.getSource() )
                && RoleMappingUserManager.class.isInstance( tmpUserManager ) )
            {
                try
                {
                    RoleMappingUserManager roleMappingUserManager = (RoleMappingUserManager) tmpUserManager;
                    Set<RoleIdentifier> roleIdentifiers =
                        roleMappingUserManager.getUsersRoles( user.getUserId(), user.getSource() );
                    if ( roleIdentifiers != null )
                    {
                        user.addAllRoles( roleIdentifiers );
                    }
                }
                catch ( UserNotFoundException e )
                {
                    logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }
    }
}
