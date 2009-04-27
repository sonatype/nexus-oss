package org.sonatype.security;

import java.util.List;
import java.util.Set;

import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
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
    public AuthorizationManager getAuthorizationManager( String sourceId );

    // *********************
    // * user management
    // *********************
    // public UserManager getUserManager( String sourceId );

    User addUser( User user );

    /**
     * Get a Subject object by id
     * 
     * @param userId
     * @return
     * @throws UserNotFoundException 
     */
    User getUser( String userId, String source ) throws UserNotFoundException;

    User updateUser( User user )
        throws UserNotFoundException;

    void deleteUser( String userId, String source )
        throws UserNotFoundException;

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

}
