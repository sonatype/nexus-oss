/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.usermanagement;

import java.util.Set;

import org.apache.shiro.realm.Realm;
import org.sonatype.configuration.validation.InvalidConfigurationException;

/**
 * A DAO for users comming from a given source.
 * 
 * @author Brian Demers
 */
public interface UserManager
{

    /**
     * Get the source string of this UserManager
     * 
     * @return
     */
    String getSource();

    /**
     * The name of the {@link Realm} is assocated with.
     * 
     * @return
     */
    String getAuthenticationRealmName();

    /**
     * If this UserManager is writable.
     * 
     * @return
     */
    boolean supportsWrite();

    /**
     * Retrieve all User objects
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

    /**
     * Add a user.
     * 
     * @param user
     * @param password
     * @return
     * @throws InvalidConfigurationException
     */
    User addUser( User user, String password )
        throws InvalidConfigurationException;

    /**
     * Update a user.
     * 
     * @param user
     * @return
     * @throws UserNotFoundException
     * @throws InvalidConfigurationException
     */
    User updateUser( User user )
        throws UserNotFoundException, InvalidConfigurationException;

    /**
     * Delete a user based on id.
     * 
     * @param userId
     * @throws UserNotFoundException
     */
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

    /**
     * Update a users password.
     * 
     * @param userId
     * @param newPassword
     * @throws UserNotFoundException
     * @throws InvalidConfigurationException
     */
    void changePassword( String userId, String newPassword )
        throws UserNotFoundException, InvalidConfigurationException;
}
