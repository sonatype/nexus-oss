/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.locators.users;

import java.util.Set;

/**
 * Handles retrieving user data from a data source
 */
public interface UserManager
{
    /**
     * Retrieve all PlexusUser objects
     * @return
     */
    Set<PlexusUser> listUsers();

    /**
     * Searches for PlexusUser objects by a criteria.
     * @return
     */
    Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria);
    
    /**
     * Retrieve all userids (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listUserIds();
    
    /**
     * Get a PlexusUser object by id
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );   
    
//    /**
//     * With multiple locators allowed, only one can be defined as primary.
//     * This is where primary data will be retrieved (Name, Email, etc.).
//     * @return
//     */
//    boolean isPrimary();
    
    /**
     * Get the source string of this locator
     * @return
     */
    String getSource();
}
