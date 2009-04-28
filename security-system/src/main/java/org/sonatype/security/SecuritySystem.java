package org.sonatype.security;

import java.util.List;
import java.util.Set;

import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.NoSuchAuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.events.SecurityEventHandler;
import org.sonatype.security.usermanagement.InvalidCredentialsException;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public interface SecuritySystem
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

    /**
     * Finds the current logged in user.
     * 
     * @return
     */
    public Subject getSubject();

    public void logout( PrincipalCollection principal );

    // *********************
    // * authorization
    // *********************
    public boolean isPermitted( PrincipalCollection principal, String permission );

    public boolean[] isPermitted( PrincipalCollection principal, List<String> permissions );

    public void checkPermission( PrincipalCollection principal, String permission )
        throws AuthorizationException;

    public void checkPermission( PrincipalCollection principal, List<String> permissions )
        throws AuthorizationException;

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
        throws NoSuchUserManager;
    
    User addUser( User user, String password )
    throws NoSuchUserManager;

    /**
     * Get a Subject object by id
     * 
     * @param userId
     * @return
     * @throws UserNotFoundException
     */
    User getUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManager;

    // FIXME: remove when https://issues.apache.org/jira/browse/KI-77, is implemented
    User getUser( String userId )
        throws UserNotFoundException;

    User updateUser( User user )
        throws UserNotFoundException,
            NoSuchUserManager;

    void deleteUser( String userId, String source )
        throws UserNotFoundException,
            NoSuchUserManager;

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

    // //
    // from PlexusSecurity
    // //

    boolean isAnonymousAccessEnabled();

    String getAnonymousUsername();

    boolean isSecurityEnabled();

    void addSecurityEventHandler( SecurityEventHandler eventHandler );

    boolean removeSecurityEventHandler( SecurityEventHandler eventHandler );
    
    // it had some user management too
    void forgotPassword( String userId, String email )
    throws UserNotFoundException;

    void forgotUsername( String email )
        throws UserNotFoundException;
    
    void resetPassword( String userId )
        throws UserNotFoundException;
    
    void changePassword( String userId, String oldPassword, String newPassword )
        throws UserNotFoundException,
            InvalidCredentialsException;
    
    void changePassword( String userId, String newPassword )
        throws UserNotFoundException;
}
