package org.sonatype.security;

import java.util.List;
import java.util.Set;

public interface SecuritySystem
{

    // *********************
    // * authentication
    // *********************

    /**
     * Authenticates a user.  If successful returns a User.
     * 
     * @param token
     * @return
     * @throws AuthenticationException if the user can not be authenticated
     */
    public User login( AuthenticationToken token )
        throws AuthenticationException;

    /**
     * Finds the current logged in user.
     * @return
     */
    public User getLoggedInUser();

    public void logout();

    // *********************
    // * authorization
    // *********************
    public boolean isAuthorized( User user, Object permission );

    public boolean[] isAuthorized( User user, List<Object> permissions );

    public void authorize( User user, Object permission )
        throws AuthorizationException;

    public void authorize( User user, List<Object> permissions )
        throws AuthorizationException;

    // *********************
    // * user management
    // *********************
    public UserManager getUserManager( String sourceId );

    public Set<User> searchUsers( SubjectSearchCriteria criteria );

}
