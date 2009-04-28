package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.security.authorization.Role;

public interface UserManager
{

    /**
     * Retrieve all Subject objects
     * 
     * @return
     */
    Set<User> listUsers();

    /**
     * Retrieve all userids (if managing full object list is to heavy handed)
     * 
     * @return
     */
    Set<String> listUserIds();

    User addUser( User user );

    User updateUser( User user )
        throws UserNotFoundException;

    void deleteUser( String userId )
        throws UserNotFoundException;

    /**
     * Roles might be stored by a different UserManager, then the one that owns the User.
     * For example, a User might come from a JDBC UserManager, but has additional roles mapped in XML.
     * 
     * @param userId
     * @param source
     * @return
     * @throws UserNotFoundException
     */
    Set<Role> getUsersRoles( String userId, String source )
        throws UserNotFoundException;

    void setUsersRoles( String userId, Set<Role> roles, String source )
        throws UserNotFoundException;

    /**
     * Searches for Subject objects by a criteria.
     * 
     * @return
     */
    Set<User> searchUsers( UserSearchCriteria criteria );

    /**
     * Get a Subject object by id
     * 
     * @param userId
     * @return
     */
    User getUser( String userId )
        throws UserNotFoundException;

    /**
     * Get the source string of this locator
     * 
     * @return
     */
    String getSource();

    boolean supportsWrite();

}
