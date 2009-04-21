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
package org.sonatype.jsecurity.locators.users;

import java.util.Set;

/**
 * The PlexusUserManager is responsible for retrieving user data from different
 * data sources.
 */
public interface PlexusUserManager
{
    public static final String SOURCE_ALL = "all";
    
    /**
     * Retrieve all PlexusUser objects defined by the PlexusUserLocator components
     * @return
     */
    Set<PlexusUser> listUsers( String source );

    /**
     * Searches for PlexusUser objects by criteria.
     * @return
     */
    Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria, String source );
    
    /**
     * Retrieve all userids defined by the PlexusUserLocator components (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listUserIds( String source );
    
    /**
     * Get a PlexusUser object by id from a PlexusUserLocator component
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );
    
    /**
     * Get a PlexusUser object by id from a PlexusUserLocator component
     * @param userId
     * @param source
     * @return
     */
    PlexusUser getUser( String userId, String source );
    
}
