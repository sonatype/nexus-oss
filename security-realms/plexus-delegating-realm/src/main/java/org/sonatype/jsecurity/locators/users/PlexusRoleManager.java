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

public interface PlexusRoleManager
{

    public static final String SOURCE_ALL = "all";
    
    /**
     * Retrieve all PlexusRole objects defined by the PlexusRoleLocator components
     * @return
     */
    Set<PlexusRole> listRoles( String source );
    
    /**
     * Retrieve all roleIds defined by the PlexusRoleLocator components (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listRoleIds( String source );

}
