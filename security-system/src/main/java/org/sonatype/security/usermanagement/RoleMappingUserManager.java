package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

/**
 * Extends the UserManager interface to allow a UserManager to add roles to users from other UserManagers. For example,
 * a User might come from a JDBC UserManager, but has additional roles mapped in XML.
 * 
 * @author Brian Demers
 */
public interface RoleMappingUserManager
    extends UserManager
{

    /**
     * Returns a list of roles for a user.
     * 
     * @param userId
     * @param userSource
     * @return
     * @throws UserNotFoundException
     */
    Set<RoleIdentifier> getUsersRoles( String userId, String userSource )
        throws UserNotFoundException;

    /**
     * Sets a users roles.
     * 
     * @param userId
     * @param userSource
     * @param roleIdentifiers
     * @throws UserNotFoundException
     * @throws InvalidConfigurationException
     */
    void setUsersRoles( String userId, String userSource, Set<RoleIdentifier> roleIdentifiers )
        throws UserNotFoundException, InvalidConfigurationException;

}
