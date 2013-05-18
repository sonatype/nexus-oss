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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * An abstract UserManager that handles filtering UserSearchCriteria in memory, this can be used in addition to an
 * external query ( if all the parameters can not be pased to the external source).
 * 
 * @author Brian Demers
 */
public abstract class AbstractUserManager
    implements UserManager
{

    protected Set<User> filterListInMemeory( Set<User> users, UserSearchCriteria criteria )
    {
        HashSet<User> result = new HashSet<User>();

        for ( User user : users )
        {
            if ( userMatchesCriteria( user, criteria ) )
            {
                // add the user if it matches the search criteria
                result.add( user );
            }
        }

        return result;
    }

    protected boolean userMatchesCriteria( User user, UserSearchCriteria criteria )
    {
        Set<String> userRoles = new HashSet<String>();
        if ( user.getRoles() != null )
        {
            for ( RoleIdentifier roleIdentifier : user.getRoles() )
            {
                userRoles.add( roleIdentifier.getRoleId() );
            }
        }

        return matchesCriteria( user.getUserId(), user.getSource(), userRoles, criteria );
    }

    protected boolean matchesCriteria( String userId, String userSource, Collection<String> usersRoles,
                                       UserSearchCriteria criteria )
    {
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !userId.toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }

        if ( criteria.getSource() != null && !criteria.getSource().equals( userSource ) )
        {
            return false;
        }

        if ( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            if ( usersRoles != null )
            {
                userRoles.addAll( usersRoles );
            }

            // check the intersection of the roles
            if ( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty() )
            {
                return false;
            }
        }

        return true;
    }

}
