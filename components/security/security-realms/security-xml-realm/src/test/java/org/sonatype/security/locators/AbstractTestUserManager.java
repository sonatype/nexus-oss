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
package org.sonatype.security.locators;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public abstract class AbstractTestUserManager
    extends AbstractReadOnlyUserManager
{

    public User getUser( String userId )
    {
        Set<User> users = this.listUsers();

        for ( User plexusUser : users )
        {
            if ( plexusUser.getUserId().equals( userId ) )
            {
                return plexusUser;
            }
        }

        return null;
    }

    public Set<String> listUserIds()
    {
        Set<String> result = new HashSet<String>();
        for ( User plexusUser : this.listUsers() )
        {
            result.add( plexusUser.getUserId() );
        }
        return result;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    protected RoleIdentifier createFakeRole( String roleId )
    {
        RoleIdentifier role = new RoleIdentifier( this.getSource(), roleId );
        return role;
    }
}
