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
package org.sonatype.jsecurity.locators;

import java.util.Set;

public interface RoleResolver
{

    /**
     * Resolves the nested Roles contained in other roles.
     * 
     * If Role A contains Z, and Role B contains X. then:<BR/>
     * <code>resolveRoles( new List( A,B ) )</code> would return <code>A, B, X, Z</code>.
     * 
     * @param roleIds
     * @return
     */
    public Set<String> resolveRoles( Set<String> roleIds);
    
    /**
     * Resolves all permissions contained Roles and nested Roles of <code>roleIds</code>.
     * 
     * @param roleIds
     * @return
     */
    public Set<String> resolvePermissions( Set<String> roleIds);
    
    /**
     * Retrieves all roles that contain this role (including itself)
     * 
     * If Role A contains Z, and Role B contains Z.  then: <BR/>
     * <code>effectiveRoles( new List( Z ) )</code> would return <code>A, B, Z</code>
     * 
     * @param roleIds
     * @return
     */
    public Set<String> effectiveRoles( Set<String> roleIds );
    
}
