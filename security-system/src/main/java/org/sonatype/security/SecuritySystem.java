package org.sonatype.security;

import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.InvalidCredentialsException;
import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public interface SecuritySystem //extends Startable
{

    // *********************
    // * authentication
    // *********************

    /**
     * Authenticates a user. If successful returns a User.
     * 
     * @param token
     * @return
     * @throws AuthenticationException if the user can not be authenticated
     */
    public Subject login( AuthenticationToken token )
        throws AuthenticationException;

    public AuthenticationInfo authenticate( AuthenticationToken token )
        throws AuthenticationException;
    
//    /**
//     * This method sets the current thread to use the <code>principal</code> passed in.
//     * You must log the user out subject.logout() when your done.
//     * 
//     * @param principal The account to login in as.
//     * @return The subject that was created as a result of the principal.
//     */
//    public Subject runAs( PrincipalCollection principal );

    /**
     * Finds the current logged in user.
     * 
     * @return
     */
    public Subject getSubject();

    public void logout( Subject subject );

    // *********************
    // * authorization
    // *********************
    public boolean isPermitted( PrincipalCollection principal, String permission );

    public boolean[] isPermitted( PrincipalCollection principal, List<String> permissions );

    public void checkPermission( PrincipalCollection principal, String permission )
        throws AuthorizationException;

    public void checkPermission( PrincipalCollection principal, List<String> permissions )
        throws AuthorizationException;

    public boolean hasRole( PrincipalCollection principals, String string );
    
    // ******************************
    // * Role permission management
    // ******************************
    public Set<Role> listRoles();

    public Set<Role> listRoles( String sourceId )
        throws NoSuchAuthorizationManager;

    // *********************
    // * user management
    // *********************
    // public UserManager getUserManager( String sourceId );

    User addUser( User user )
        throws NoSuchUserManagerException,
            InvalidConfigurationException;

    User addUser( User user, String password )
        throws NoSuchUserManagerException,
            InvalidConfigurationException;

    /**
     * Get a Subject object by id
     * 
     * @param userId
     * @return
     * @throws UserNotFoundException
     */
    User getUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManagerException;

    // TODOE: remove when https://issues.apache.org/jira/browse/KI-77, is implemented
    // I think we might need to keep this one around...
    User getUser( String userId )
        throws UserNotFoundException;

    User updateUser( User user )
        throws UserNotFoundException,
            NoSuchUserManagerException,
            InvalidConfigurationException;

    void deleteUser( String userId )
        throws UserNotFoundException;

    void deleteUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManagerException;

    Set<RoleIdentifier> getUsersRoles( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManagerException;

    void setUsersRoles( String userId, String source, Set<RoleIdentifier> roleIdentifiers )
        throws InvalidConfigurationException,
            UserNotFoundException;

    /**
     * Retrieve all Subject objects
     * 
     * @return
     */
    Set<User> listUsers();

    /**
     * Searches for Subject objects by a criteria.
     * 
     * @return
     */
    public Set<User> searchUsers( UserSearchCriteria criteria );

    // forget / change password
    void forgotPassword( String userId, String email )
        throws UserNotFoundException,
            InvalidConfigurationException;

    void forgotUsername( String email )
        throws UserNotFoundException;

    void resetPassword( String userId )
        throws UserNotFoundException,
            InvalidConfigurationException;

    void changePassword( String userId, String oldPassword, String newPassword )
        throws UserNotFoundException,
            InvalidCredentialsException,
            InvalidConfigurationException;

    void changePassword( String userId, String newPassword )
        throws UserNotFoundException,
            InvalidConfigurationException;

    // //
    // Authorization Management
    // //
    public Set<Privilege> listPrivileges();

    public AuthorizationManager getAuthorizationManager( String source )
        throws NoSuchAuthorizationManager;

    // //
    // Application configuration, TODO: I don't think all of these need to be exposed, but they currently are
    // //

    List<String> getRealms();

    void setRealms( List<String> realms ) throws InvalidConfigurationException;

    boolean isAnonymousAccessEnabled();

    void setAnonymousAccessEnabled( boolean enabled );

    String getAnonymousUsername();

    void setAnonymousUsername( String anonymousUsername ) throws InvalidConfigurationException;
    
    String getAnonymousPassword();

    void setAnonymousPassword( String anonymousPassword ) throws InvalidConfigurationException;

    boolean isSecurityEnabled();

    void setSecurityEnabled( boolean enabled );
    
    void start()
    throws StartingException;

    void stop()
    throws StoppingException;
}
