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
package org.sonatype.security.locators;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.AbstractUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public abstract class AbstractTestUserManager
    extends AbstractUserManager
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

    public boolean isPrimary()
    {
        return false;
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

    protected Role createFakeRole( String roleId )
    {
        Role role = new Role();
        role.setName( roleId );
        role.setRoleId( roleId );
        role.setSource( this.getSource() );

        return role;
    }

    public boolean supportsWrite()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public User addUser( User user )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub

    }

    public Set<Role> getUsersRoles( String userId, String source )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setUsersRoles( String userId, Set<Role> roles, String source )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub

    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
