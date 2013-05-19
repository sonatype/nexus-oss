/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
