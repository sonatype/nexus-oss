package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

public interface RoleMappingUserManager extends UserManager
{

    /**
     * Roles might be stored by a different UserManager, then the one that owns the User. For example, a User might come
     * from a JDBC UserManager, but has additional roles mapped in XML.
     * 
     * @param userId
     * @param userSource
     * @return
     * @throws UserNotFoundException
     */
    Set<RoleIdentifier> getUsersRoles( String userId, String userSource )
        throws UserNotFoundException;

    void setUsersRoles( String userId, String userSource, Set<RoleIdentifier> roleIdentifiers )
        throws UserNotFoundException, InvalidConfigurationException;
    
}
