package org.sonatype.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.SecurityUtils;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.cache.Cache;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.DelegatingSubject;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.jsecurity.util.ThreadContext;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.configuration.SecurityConfigurationManager;
import org.sonatype.security.email.NullSecurityEmailer;
import org.sonatype.security.email.SecurityEmailer;
import org.sonatype.security.events.AuthorizationConfigurationChangedEvent;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;
import org.sonatype.security.usermanagement.InvalidCredentialsException;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.PasswordGenerator;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.RoleMappingUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

/**
 * This implementation wraps a jsecurity/Ki SecurityManager, and adds user management.
 */
@Component( role = SecuritySystem.class )
public class DefaultSecuritySystem
    implements SecuritySystem, Initializable, EventListener
{
    @Requirement
    private SecurityConfigurationManager securityConfiguration;

    // TODO: we should get this value from the config, (not plexus)
    @Configuration( value = "web" )
    private String applicationSecurityManagerHint;

    @Configuration( value = "default" )
    private String runAsSecurityManagerHint;

    @Requirement( role = RealmSecurityManager.class )
    private Map<String, RealmSecurityManager> securityManagerMap;

    @Requirement( role = UserManager.class )
    private Map<String, UserManager> userManagerMap;

    @Requirement
    private PlexusContainer container;

    @Requirement( role = AuthorizationManager.class )
    private Map<String, AuthorizationManager> authorizationManagers;

    @Requirement
    private PasswordGenerator passwordGenerator;

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;

    private SecurityEmailer securityEmailer;

    @Requirement
    private Logger logger;

    private static final String ALL_ROLES_KEY = "all";

    public Subject login( AuthenticationToken token )
        throws AuthenticationException
    {
        try
        {
            return this.getApplicationSecurityManager().login( token );
        }
        catch ( org.jsecurity.authc.AuthenticationException e )
        {
            throw new AuthenticationException( e.getMessage(), e );
        }
    }

    public AuthenticationInfo authenticate( AuthenticationToken token )
        throws AuthenticationException
    {
        try
        {
            return this.getApplicationSecurityManager().authenticate( token );
        }
        catch ( org.jsecurity.authc.AuthenticationException e )
        {
            throw new AuthenticationException( e.getMessage(), e );
        }
    }

    public Subject runAs( PrincipalCollection principal )
    {
        RealmSecurityManager securityManager = this.getRunAsSecurityManager();
        // TODO: we might need to bind this to the ThreadContext for this thread
        // however if we do this we would need to unbind it so it doesn't leak
        // ThreadContext.bind( securityManager );

        DelegatingSubject fakeLoggedInSubject = new DelegatingSubject( principal, true, null, null, securityManager );

        // fake the login
        ThreadContext.bind( fakeLoggedInSubject );
        // this is un-bind when the user logs out.

        return fakeLoggedInSubject;
    }

    public Subject getSubject()
    {
        // this gets the currently bound Subject to the thread
        return SecurityUtils.getSubject();
    }

    public void logout( PrincipalCollection principal )
    {
        this.getApplicationSecurityManager().logout( principal );
    }

    public boolean isPermitted( PrincipalCollection principal, String permission )
    {
        return this.getApplicationSecurityManager().isPermitted( principal, permission );
    }

    public boolean[] isPermitted( PrincipalCollection principal, List<String> permissions )
    {
        return this.getApplicationSecurityManager().isPermitted( principal,
            permissions.toArray( new String[permissions.size()] ) );
    }

    public void checkPermission( PrincipalCollection principal, String permission )
        throws AuthorizationException
    {
        try
        {
            this.getApplicationSecurityManager().checkPermission( principal, permission );
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
            this.getApplicationSecurityManager().checkPermissions( principal,
                permissions.toArray( new String[permissions.size()] ) );
        }
        catch ( org.jsecurity.authz.AuthorizationException e )
        {
            throw new AuthorizationException( e.getMessage(), e );
        }
    }

    public boolean hasRole( PrincipalCollection principals, String string )
    {
        return this.getApplicationSecurityManager().hasRole( principals, string );
    }

    private Collection<Realm> getRealmsFromConfigSource()
    {
        List<Realm> realms = new ArrayList<Realm>();

        List<String> realmIds = this.securityConfiguration.getRealms();

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

    public Set<Role> listRoles( String sourceId )
        throws NoSuchAuthorizationManager
    {
        if ( ALL_ROLES_KEY.equalsIgnoreCase( sourceId ) )
        {
            return this.listRoles();
        }
        else
        {
            AuthorizationManager authzManager = this.getAuthorizationManager( sourceId );
            return authzManager.listRoles();
        }
    }

    public Set<Privilege> listPrivileges()
    {
        Set<Privilege> privileges = new HashSet<Privilege>();
        for ( AuthorizationManager authzManager : this.authorizationManagers.values() )
        {
            Set<Privilege> tmpPrivileges = authzManager.listPrivileges();
            if ( tmpPrivileges != null )
            {
                privileges.addAll( tmpPrivileges );
            }
        }

        return privileges;
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
        throws NoSuchUserManager, InvalidConfigurationException
    {
        return this.addUser( user, this.generatePassword() );
    }

    public User addUser( User user, String password )
        throws NoSuchUserManager, InvalidConfigurationException
    {
        // if the password is null, generate one
        if ( password == null )
        {
            password = this.generatePassword();
        }

        // first save the user
        // this is the UserManager that owns the user
        UserManager userManager = this.getUserManager( user.getSource() );

        if ( !userManager.supportsWrite() )
        {
            throw new InvalidConfigurationException( "UserManager: " + userManager.getSource()
                + " does not support writing." );
        }

        userManager.addUser( user, password );

        // then save the users Roles
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only save roles
            if ( !tmpUserManager.getSource().equals( user.getSource() )
                && RoleMappingUserManager.class.isInstance( tmpUserManager ) )
            {
                try
                {
                    RoleMappingUserManager roleMappingUserManager = (RoleMappingUserManager) tmpUserManager;
                    roleMappingUserManager.setUsersRoles( user.getUserId(), user.getSource(), RoleIdentifier
                        .getRoleIdentifiersForSource( user.getSource(), user.getRoles() ) );
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }

        if ( UserStatus.active.equals( user.getStatus() ) )
        {
            // don't forget to email the user (if the user being added is active)!
            getSecurityEmailer().sendNewUserCreated( user.getEmailAddress(), user.getUserId(), password );
        }

        return user;
    }

    public User updateUser( User user )
        throws UserNotFoundException, NoSuchUserManager, InvalidConfigurationException
    {
        // first update the user
        // this is the UserManager that owns the user
        UserManager userManager = this.getUserManager( user.getSource() );

        if ( !userManager.supportsWrite() )
        {
            throw new InvalidConfigurationException( "UserManager: " + userManager.getSource()
                + " does not support writing." );
        }

        userManager.updateUser( user );

        // then save the users Roles
        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            // skip the user manager that owns the user, we already did that
            // these user managers will only save roles
            if ( !tmpUserManager.getSource().equals( user.getSource() )
                && RoleMappingUserManager.class.isInstance( tmpUserManager ) )
            {
                try
                {
                    RoleMappingUserManager roleMappingUserManager = (RoleMappingUserManager) tmpUserManager;
                    roleMappingUserManager.setUsersRoles( user.getUserId(), user.getSource(), RoleIdentifier
                        .getRoleIdentifiersForSource( user.getSource(), user.getRoles() ) );
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }

        // clear the realm caches
        this.eventMulticaster.notifyEventListeners( new AuthorizationConfigurationChangedEvent( null ) );

        return user;
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {
        User user = this.getUser( userId );
        try
        {
            this.deleteUser( userId, user.getSource() );
        }
        catch ( NoSuchUserManager e )
        {
            this.logger.error( "User manager returned user, but could not be found: " + e.getMessage(), e );
            throw new IllegalStateException( "User manager returned user, but could not be found: " + e.getMessage(), e );
        }
    }

    public void deleteUser( String userId, String source )
        throws UserNotFoundException, NoSuchUserManager
    {
        UserManager userManager = this.getUserManager( source );
        userManager.deleteUser( userId );
    }

    public Set<RoleIdentifier> getUsersRoles( String userId, String source )
        throws UserNotFoundException, NoSuchUserManager
    {
        User user = this.getUser( userId, source );
        return user.getRoles();
    }

    public void setUsersRoles( String userId, String source, Set<RoleIdentifier> roleIdentifiers )
        throws InvalidConfigurationException, UserNotFoundException
    {
        // TODO: this is a bit sticky, what we really want to do is just expose the RoleMappingUserManagers this way (i
        // think), maybe this is too generic

        boolean foundUser = false;

        for ( UserManager tmpUserManager : this.userManagerMap.values() )
        {
            if ( RoleMappingUserManager.class.isInstance( tmpUserManager ) )
            {
                RoleMappingUserManager roleMappingUserManager = (RoleMappingUserManager) tmpUserManager;
                try
                {
                    foundUser = true;
                    roleMappingUserManager.setUsersRoles( userId, source, RoleIdentifier.getRoleIdentifiersForSource(
                        tmpUserManager.getSource(), roleIdentifiers ) );
                }
                catch ( UserNotFoundException e )
                {
                    this.logger.debug( "User '" + userId + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }

        if ( !foundUser )
        {
            throw new UserNotFoundException( userId );
        }
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
        throws UserNotFoundException, NoSuchUserManager
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

        // FIXME This doesn't seem right... why is this like this
        // NOTE: if we want to leave this very generic we need to search ALL UserManagers even if the source is set
        // the problem is that some users could be found by looking up role mappings in other realms

        if( StringUtils.isEmpty( criteria.getSource() ) )
        {
         // search all user managers
            for ( UserManager tmpUserManager : this.userManagerMap.values() )
            {
                users.addAll( tmpUserManager.searchUsers( criteria ) );
            }
        }
        else
        {
            try
            {
                users.addAll( this.getUserManager( criteria.getSource() ).searchUsers( criteria ) );
            }
            catch ( NoSuchUserManager e )
            {
                this.logger.warn( "UserManager: "+ criteria.getSource() +" was not found.", e );
            }
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
    private List<UserManager> orderUserManagers()
    {
        List<UserManager> orderedLocators = new ArrayList<UserManager>();

        List<UserManager> unOrderdLocators = new ArrayList<UserManager>( this.userManagerMap.values() );

        Map<String, UserManager> realmToUserManagerMap = new HashMap<String, UserManager>();

        for ( UserManager userManager : this.userManagerMap.values() )
        {
            if ( userManager.getAuthenticationRealmName() != null )
            {
                realmToUserManagerMap.put( userManager.getAuthenticationRealmName(), userManager );
            }
        }

        // get the sorted order of realms from the realm locator
        Collection<Realm> realms = this.getApplicationSecurityManager().getRealms();

        for ( Realm realm : realms )
        {
            // now user the realm.name to find the UserManager
            if ( realmToUserManagerMap.containsKey( realm.getName() ) )
            {
                UserManager userManager = realmToUserManagerMap.get( realm.getName() );
                // remove from unorderd and add to orderd
                unOrderdLocators.remove( userManager );
                orderedLocators.add( userManager );
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
                    this.logger.debug( "User '" + user.getUserId() + "' is not managed by the usermanager: "
                        + tmpUserManager.getSource() );
                }
            }
        }
    }

    public AuthorizationManager getAuthorizationManager( String source )
        throws NoSuchAuthorizationManager
    {
        if ( !this.authorizationManagers.containsKey( source ) )
        {
            throw new NoSuchAuthorizationManager( "AuthorizationManager with source: '" + source
                + "' could not be found." );
        }

        return this.authorizationManagers.get( source );
    }

    public String getAnonymousUsername()
    {
        return this.securityConfiguration.getAnonymousUsername();
    }

    public boolean isAnonymousAccessEnabled()
    {
        return this.securityConfiguration.isAnonymousAccessEnabled();
    }

    public boolean isSecurityEnabled()
    {
        return this.securityConfiguration.isEnabled();
    }

    public void changePassword( String userId, String oldPassword, String newPassword )
        throws UserNotFoundException, InvalidCredentialsException, InvalidConfigurationException
    {
        // first authenticate the user
        try
        {
            UsernamePasswordToken authenticationToken = new UsernamePasswordToken( userId, oldPassword );
            if ( this.getApplicationSecurityManager().authenticate( authenticationToken ) == null )
            {
                throw new InvalidCredentialsException();
            }
        }
        catch ( org.jsecurity.authc.AuthenticationException e )
        {
            this.logger.debug( "User failed to change password reason: " + e.getMessage(), e );
            throw new InvalidCredentialsException();
        }

        // if that was good just change the password
        this.changePassword( userId, newPassword );
    }

    public void changePassword( String userId, String newPassword )
        throws UserNotFoundException, InvalidConfigurationException
    {
        User user = this.getUser( userId );

        try
        {
            UserManager userManager = this.getUserManager( user.getSource() );
            userManager.changePassword( userId, newPassword );
        }
        catch ( NoSuchUserManager e )
        {
            // this should NEVER happen
            this.logger.warn( "User '" + userId + "' with source: '" + user.getSource()
                + "' but could not find the UserManager for that source." );
        }

    }

    public void forgotPassword( String userId, String email )
        throws UserNotFoundException, InvalidConfigurationException
    {
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setEmail( email );
        criteria.setUserId( userId );

        Set<User> users = this.searchUsers( criteria );

        boolean found = false;

        for ( User user : users )
        {
            // TODO: criteria does not do exact matching
            if ( user.getUserId().equalsIgnoreCase( userId.trim() ) && user.getEmailAddress().equals( email ) )
            {
                found = true;
                break;
            }
        }

        if ( !found )
        {
            throw new UserNotFoundException( email );
        }

        resetPassword( userId );
    }

    public void forgotUsername( String email )
        throws UserNotFoundException
    {
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setEmail( email );

        Set<User> users = this.searchUsers( criteria );

        List<String> userIds = new ArrayList<String>();
        for ( User user : users )
        {
            // ignore the anon user
            if ( !user.getUserId().equalsIgnoreCase( this.getAnonymousUsername() )
                && email.equalsIgnoreCase( user.getEmailAddress() ) )
            {
                userIds.add( user.getUserId() );
            }
        }

        if ( userIds.size() > 0 )
        {

            this.getSecurityEmailer().sendForgotUsername( email, userIds );
        }
        else
        {
            throw new UserNotFoundException( email );
        }

    }

    public void resetPassword( String userId )
        throws UserNotFoundException, InvalidConfigurationException
    {
        String newClearTextPassword = this.generatePassword();

        User user = this.getUser( userId );

        this.changePassword( userId, newClearTextPassword );

        // send email
        this.getSecurityEmailer().sendResetPassword( user.getEmailAddress(), newClearTextPassword );

    }

    private String generatePassword()
    {
        return this.passwordGenerator.generatePassword( 10, 10 );
    }

    private SecurityEmailer getSecurityEmailer()
    {
        if ( this.securityEmailer == null )
        {
            try
            {
                this.securityEmailer = this.container.lookup( SecurityEmailer.class );
            }
            catch ( ComponentLookupException e )
            {
                this.logger.error( "Failed to find a SecurityEmailer" );
                this.securityEmailer = new NullSecurityEmailer();
            }
        }
        return this.securityEmailer;
    }

    public List<String> getRealms()
    {
        return new ArrayList<String>( this.securityConfiguration.getRealms() );
    }

    public void setRealms( List<String> realms )
        throws InvalidConfigurationException
    {
        this.securityConfiguration.setRealms( realms );
        this.securityConfiguration.save();

        // update the realms in the security manager
        for ( RealmSecurityManager securityManager : this.securityManagerMap.values() )
        {
            securityManager.setRealms( new ArrayList<Realm>( this.getRealmsFromConfigSource() ) );
        }
    }

    public void setAnonymousAccessEnabled( boolean enabled )
    {
        this.securityConfiguration.setAnonymousAccessEnabled( enabled );
        this.securityConfiguration.save();
    }

    public void setAnonymousUsername( String anonymousUsername )
        throws InvalidConfigurationException
    {
        this.securityConfiguration.setAnonymousUsername( anonymousUsername );
        this.securityConfiguration.save();
    }

    public void setSecurityEnabled( boolean enabled )
    {
        this.securityConfiguration.setEnabled( enabled );
        this.securityConfiguration.save();
    }

    public String getAnonymousPassword()
    {
        return this.securityConfiguration.getAnonymousPassword();
    }

    public void setAnonymousPassword( String anonymousPassword )
        throws InvalidConfigurationException
    {
        this.securityConfiguration.setAnonymousPassword( anonymousPassword );
        this.securityConfiguration.save();
    }

    public void start()
        throws StartingException
    {
        // reload the config
        this.securityConfiguration.clearCache();
        this.clearRealmCaches();

        for ( RealmSecurityManager securityManager : this.securityManagerMap.values() )
        {
            securityManager.setRealms( new ArrayList<Realm>( this.getRealmsFromConfigSource() ) );
        }
    }

    public void stop()
        throws StoppingException
    {
        // we don't have anything to do on stop
    }

    private void clearRealmCaches()
    {
        // NOTE: we don't need to iterate all the Sec Managers, they use the same Realms, so one is fine.
        if ( this.getApplicationSecurityManager().getRealms() != null )
        {
            for ( Realm realm : this.getApplicationSecurityManager().getRealms() )
            {
                // check if its a AuthorizingRealm, if so clear the cache
                if ( AuthorizingRealm.class.isInstance( realm ) )
                {
                    // clear the cache
                    AuthorizingRealm aRealm = (AuthorizingRealm) realm;

                    Cache cache = aRealm.getAuthorizationCache();
                    if ( cache != null )
                    {
                        cache.clear();
                    }
                }
            }
        }
    }

    public void onEvent( Event<?> evt )
    {
        if ( AuthorizationConfigurationChangedEvent.class.isInstance( evt ) )
        {
            this.clearRealmCaches();
        }

        if ( SecurityConfigurationChangedEvent.class.isInstance( evt ) )
        {
            this.clearRealmCaches();
            this.securityConfiguration.clearCache();

            for ( RealmSecurityManager securityManager : this.securityManagerMap.values() )
            {
                securityManager.setRealms( new ArrayList<Realm>( this.getRealmsFromConfigSource() ) );
            }
        }
    }

    public void initialize()
        throws InitializationException
    {
        // add event handler
        this.eventMulticaster.addEventListener( this );

    }

    private RealmSecurityManager getApplicationSecurityManager()
    {
        return this.securityManagerMap.get( this.applicationSecurityManagerHint );
    }

    private RealmSecurityManager getRunAsSecurityManager()
    {
        return this.securityManagerMap.get( this.runAsSecurityManagerHint );
    }
}
