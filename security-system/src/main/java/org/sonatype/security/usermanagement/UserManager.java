package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

public interface UserManager
{

    /**
     * Get the source string of this UserManager
     * 
     * @return
     */
    String getSource();
    
    String getAuthenticationRealmName();

    boolean supportsWrite();

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

    User addUser( User user, String password ) throws InvalidConfigurationException;

    User updateUser( User user )
        throws UserNotFoundException, InvalidConfigurationException;

    void deleteUser( String userId )
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

    void changePassword( String userId, String newPassword )
        throws UserNotFoundException, InvalidConfigurationException;
}
