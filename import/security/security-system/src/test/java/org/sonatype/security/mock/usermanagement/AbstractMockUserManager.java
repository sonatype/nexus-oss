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
package org.sonatype.security.mock.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.security.usermanagement.AbstractUserManager;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public abstract class AbstractMockUserManager
    extends AbstractUserManager
{

    private Set<User> users = new HashSet<User>();

    public boolean supportsWrite()
    {
        return true;
    }

    public User addUser( User user, String password )
    {
        this.getUsers().add( user );
        return user;
    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        User existingUser = this.getUser( user.getUserId() );

        if ( existingUser == null )
        {
            throw new UserNotFoundException( user.getUserId() );
        }

        return user;
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {
        User existingUser = this.getUser( userId );

        if ( existingUser == null )
        {
            throw new UserNotFoundException( userId );
        }

        this.getUsers().remove( existingUser );

    }

    public User getUser( String userId )
    {
        for ( User user : this.getUsers() )
        {
            if ( user.getUserId().equals( userId ) )
            {
                return user;
            }
        }
        return null;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( User user : this.getUsers() )
        {
            userIds.add( user.getUserId() );
        }

        return userIds;
    }

    public Set<User> listUsers()
    {
        return Collections.unmodifiableSet( this.getUsers() );
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.getUsers(), criteria );
    }

    protected Set<User> getUsers()
    {
        return users;
    }

    protected void setUsers( Set<User> users )
    {
        this.users = users;
    }

    public Set<RoleIdentifier> getUsersRoles( String userId, String source )
        throws UserNotFoundException
    {
        return null;
    }

    public void changePassword( String userId, String newPassword )
        throws UserNotFoundException
    {
    }
}
