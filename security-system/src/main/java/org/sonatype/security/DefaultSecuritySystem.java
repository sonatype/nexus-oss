package org.sonatype.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

/**
 * This implementation wraps a jsecurity/Ki SecurityManager, and adds user management.
 */
@Component( role = SecuritySystem.class )
public class DefaultSecuritySystem
    implements SecuritySystem, Initializable
{
    @Requirement( hint = "file" )
    private SecurityConfigurationSource configSource;

    @Requirement
    private RealmSecurityManager securityManager;

    @Requirement( role = UserManager.class )
    private Map<String, UserManager> userManagerMap;

    @Requirement
    private PlexusContainer container;

    @Requirement( role = AuthorizationManager.class )
    private Map<String, AuthorizationManager> authorizationManagers;

    @Requirement
    private Logger logger;

    public Subject login( AuthenticationToken token )
        throws AuthenticationException
    {
        try
        {
            return this.securityManager.login( token );
        }
        catch ( org.jsecurity.authc.AuthenticationException e )
        {
            throw new AuthenticationException( e.getMessage(), e );
        }
    }

    public Subject getSubject()
    {
        return this.securityManager.getSubject();
    }

    public void logout( PrincipalCollection principal )
    {
        this.securityManager.logout( principal );
    }

    public boolean isPermitted( PrincipalCollection principal, String permission )
    {
        return this.securityManager.isPermitted( principal, permission );
    }

    public boolean[] isPermitted( PrincipalCollection principal, List<String> permissions )
    {
        return this.securityManager.isPermitted( principal, permissions.toArray( new String[permissions.size()] ) );
    }

    public void checkPermission( PrincipalCollection principal, String permission )
        throws AuthorizationException
    {
        try
        {
            this.securityManager.checkPermission( principal, permission );
        }
        catch ( org.jsecurity.authz.AuthorizationException e )
        {
            throw new AuthorizationException( e.getMessage(), e );
        }

    }

    public void checkPermission( PrincipalCollection principal, List<String> permissions )
        throws AuthorizationException
    {
        try
        {
            this.securityManager.checkPermissions( principal, permissions.toArray( new String[permissions.size()] ) );
        }
        catch ( org.jsecurity.authz.AuthorizationException e )
        {
            throw new AuthorizationException( e.getMessage(), e );
        }
    }

    public void initialize()
        throws InitializationException
    {
        // load the configuration
        try
        {
            this.configSource.loadConfiguration();

            if ( this.configSource.getConfiguration() == null )
            {
                throw new InitializationException( "Failed to load the security configuration." );
            }

            this.securityManager.setRealms( new ArrayList<Realm>( this.getRealmsFromConfigSource() ) );
        }
        catch ( Exception e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
    }

    private Collection<Realm> getRealmsFromConfigSource()
    {
        Set<Realm> realms = new HashSet<Realm>();

        List<String> realmIds = this.configSource.getConfiguration().getRealms();

        for ( String realmId : realmIds )
        {
            try
            {
                // First will load from plexus container
                realms.add( (Realm) container.lookup( Realm.class, realmId ) );
            }
            catch ( ComponentLookupException e )
            {
                this.logger.debug( "Failed to look up realm as plexus component, trying a Class.forName()", e );
                // If that fails, will simply use reflection to load
                try
                {
                    realms.add( (Realm) Class.forName( realmId ).newInstance() );
                }
                catch ( Exception e1 )
                {
                    this.logger.error( "Unable to lookup security realms", e );
                }
            }
        }

        return realms;
    }

    public Set<Role> listRoles()
    {
        Set<Role> roles = new HashSet<Role>();
        for ( AuthorizationManager authzManager : this.authorizationManagers.values() )
        {
            Set<Role> tmpRoles = authzManager.listRoles();
            if ( tmpRoles != null )
            {
                roles.addAll( tmpRoles );
            }
        }

        return roles;
    }

    public Set<Role> listRoles( String sourceId ) throws NoSuchAuthorizationManager
    {
        AuthorizationManager authzManager = this.getAuthorizationManager( sourceId );
        return authzManager.listRoles();
    }

    // *********************
    // * user management
    // *********************

    private UserManager getUserManager( String sourceId )
        throws NoSuchUserManager
    {
        if ( !this.userManagerMap.containsKey( sourceId ) )
        {
            throw new NoSuchUserManager( "UserManager with source: '" + sourceId + "' could not be found." );
        }

        return this.userManagerMap.get( sourceId );
    }

    public User addUser( User user )
        throws NoSuchUserManager
    {
        // first save the user
        // this is the UserManager that owns the user
        UserManager userManager = this.getUserManager( user.getSource() );
        userManager.addUser( user );

        // then save the users Roles
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only save roles
            if ( !tmpUserManager.getSource().equals( user.getSource() ) )
            {
                try
                {
                    tmpUserManager.setUsersRoles( user.getUserId(), user.getRoles(), user.getSource() );
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }

        return user;
    }

    public User updateUser( User user )
        throws UserNotFoundException,
            NoSuchUserManager
    {
        // first update the user
        // this is the UserManager that owns the user
        UserManager userManager = this.getUserManager( user.getSource() );

        userManager.updateUser( user );

        // then save the users Roles
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only save roles
            if ( !tmpUserManager.getSource().equals( user.getSource() ) )
            {
                try
                {
                    tmpUserManager.setUsersRoles( user.getUserId(), user.getRoles(), user.getSource() );
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }

        return user;
    }

    public void deleteUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManager
    {
        UserManager userManager = this.getUserManager( source );
        userManager.deleteUser( userId );
    }

    public User getUser( String userId )
        throws UserNotFoundException
    {
        List<UserManager> orderedUserManagers = this.orderUserManagers();
        for ( UserManager userManager : orderedUserManagers )
        {
            try
            {
                return this.getUser( userId, userManager.getSource() );
            }
            catch ( UserNotFoundException e )
            {
                this.logger.debug( "User: '" + userId + "' was not found in: '" + userManager.getSource() + "' " );
            }
            catch ( NoSuchUserManager e )
            {
                // we should NEVER bet here
                this.logger.warn( "UserManager: '" + userManager.getSource()
                    + "' was not found, but is in the list of UserManagers", e );
            }
        }
        throw new UserNotFoundException( userId );
    }

    public User getUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManager
    {
        // first get the user
        // this is the UserManager that owns the user
        UserManager userManager = this.getUserManager( source );
        User user = userManager.getUser( userId );

        if ( user == null )
        {
            throw new UserNotFoundException( userId );
        }

        // add roles from other user managers
        this.addOtherRolesToUser( user );

        return user;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            users.addAll( tmpUserManager.listUsers() );
        }

        // now add all the roles to the users
        for ( User user : users )
        {
            // add roles from other user managers
            this.addOtherRolesToUser( user );
        }

        return users;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        Set<User> users = new HashSet<User>();

        // NOTE: if we want to leave this very generic we need to search ALL UserManagers even if the source is set
        // the problem is that some users could be found by looking up role mappings in other realms

        // search all user managers
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            users.addAll( tmpUserManager.searchUsers( criteria ) );
        }

        // now add all the roles to the users
        for ( User user : users )
        {
            // add roles from other user managers
            this.addOtherRolesToUser( user );
        }

        return users;
    }

    /**
     * We need to order the UserManagers the same way as the Realms are ordered. We need to be able to find a user based
     * on the ID. This my never go away, but the current reason why we need it is:
     * https://issues.apache.org/jira/browse/KI-77 There is no (clean) way to resolve a realms roles into permissions.
     * take a look at the issue and VOTE!
     * 
     * @return the list of UserManagers in the order (as close as possible) to the list of realms.
     */
    @SuppressWarnings( "unchecked" )
    private List<UserManager> orderUserManagers()
    {
        List<UserManager> orderedLocators = new ArrayList<UserManager>();

        List<UserManager> unOrderdLocators = new ArrayList<UserManager>( this.userManagerMap.values() );

        // get the sorted order of realms from the realm locator
        Collection<Realm> realms = this.securityManager.getRealms();

        for ( Realm realm : realms )
        {
            try
            {
                UserManager userManager = this.getUserManager( realm.getName() );
                // remove from unorderd and add to orderd
                unOrderdLocators.remove( userManager );
                orderedLocators.add( userManager );
                break;
            }
            catch ( NoSuchUserManager e )
            {
                this.logger.debug( "Could not find a UserManager for realm: " + realm.getClass() + " name: "
                    + realm.getName() );
            }
        }

        // now add all the un-ordered ones to the ordered ones, this way they will be at the end of the ordered list
        orderedLocators.addAll( unOrderdLocators );

        return orderedLocators;

    }

    private void addOtherRolesToUser( User user )
    {
        // then save the users Roles
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only have roles
            if ( !tmpUserManager.getSource().equals( user.getSource() ) )
            {
                try
                {
                    Set<Role> roles = tmpUserManager.getUsersRoles( user.getUserId(), user.getSource() );
                    if ( roles != null )
                    {
                        user.getRoles().addAll( roles );
                    }
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }
    }

    private AuthorizationManager getAuthorizationManager( String source ) throws NoSuchAuthorizationManager
    {
        if ( !this.authorizationManagers.containsKey( source ) )
        {
            throw new NoSuchAuthorizationManager("AuthorizationManager with source: '" + source + "' could not be found."  );
        }

        return this.authorizationManagers.get( source );
    }
}
