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
 * Handles retrieving role data from a data source.
 */
public interface PlexusRoleLocator
{
    /**
     * Retrieve all role Ids (if managing full object
     * list is to heavy handed).
     * 
     * @return A Set of all Role Ids from this source.
     */
    public Set<String> listRoleIds();
    
    /**
     * Retrieve all roles from this source.
     * 
     * @return All PlexusRole from this source.
     */
    public Set<PlexusRole> listRoles();
    
    /**
     * Get the source string of this locator
     * @return
     */
    public String getSource();
    
}
