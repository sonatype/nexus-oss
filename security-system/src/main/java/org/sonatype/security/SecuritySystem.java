package org.sonatype.security;

import java.util.List;
import java.util.Set;

import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.users.User;
import org.sonatype.security.users.UserManager;
import org.sonatype.security.users.UserSearchCriteria;

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
    public Subject login( AuthenticationToken token )
        throws AuthenticationException;

    /**
     * Finds the current logged in user.
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

    // *********************
    // * user management
    // *********************
    public UserManager getUserManager( String sourceId );

    public Set<User> searchUsers( UserSearchCriteria criteria );
    
    // ******************************
    // * Role permission management
    // ******************************  
    public AuthorizationManager getAuthorizationManager( String sourceId );

}
