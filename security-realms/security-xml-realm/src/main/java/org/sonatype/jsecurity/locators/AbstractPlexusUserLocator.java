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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.UserManager;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;

public abstract class AbstractPlexusUserLocator
    implements UserManager
{
    
    protected Set<PlexusUser> filterListInMemeory( Set<PlexusUser> users, PlexusUserSearchCriteria criteria )
    {
        HashSet<PlexusUser> result = new HashSet<PlexusUser>();

        for ( PlexusUser user : users )
        {
            if ( userMatchesCriteria( user, criteria ) )
            {
                // add the user if it matches the search criteria
                result.add( user );
            }
        }

        return result;
    }

    protected boolean userMatchesCriteria( PlexusUser user, PlexusUserSearchCriteria criteria )
    {
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !user.getUserId().toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }
        
        if( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            for ( PlexusRole role : user.getRoles() )
            {
                userRoles.add( role.getRoleId() );
            }
            
            // check the intersection of the roles
            if( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty())
            {
                return false;
            }
        }
        
        return true;
    }
}
