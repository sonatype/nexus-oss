package org.sonatype.security;

import java.util.Set;

public interface UserManager
{

    /**
     * Retrieve all Subject objects
     * @return
     */
    Set<User> listUsers();

    /**
     * Searches for Subject objects by a criteria.
     * @return
     */
    Set<User> searchUsers( SubjectSearchCriteria criteria);
    
    /**
     * Retrieve all userids (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listUserIds();
    
    /**
     * Get a Subject object by id
     * @param userId
     * @return
     */
    User getUser( String userId );   
    
    /**
     * Get the source string of this locator
     * @return
     */
    String getSource();
    
}
