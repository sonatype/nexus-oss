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
package org.sonatype.security.mock;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

@Singleton
@Named( "Mock" )
@Typed( UserManager.class )
public class MockUserManager
    extends AbstractReadOnlyUserManager
{

    public String getSource()
    {
        return "Mock";
    }

    public String getAuthenticationRealmName()
    {
        return "Mock";
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        User jcohen = new DefaultUser();
        jcohen.setEmailAddress( "JamesDCohen@example.com" );
        jcohen.setFirstName( "James" );
        jcohen.setLastName( "Cohen" );
        // jcohen.setName( "James E. Cohen" );
        // jcohen.setReadOnly( true );
        jcohen.setSource( "Mock" );
        jcohen.setStatus( UserStatus.active );
        jcohen.setUserId( "jcohen" );
        jcohen.addRole( new RoleIdentifier( "Mock", "mockrole1" ) );
        users.add( jcohen );

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();
        for ( User user : this.listUsers() )
        {
            userIds.add( user.getUserId() );
        }
        return userIds;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return null;
    }

    public User getUser( String userId )
        throws UserNotFoundException
    {
        for ( User user : this.listUsers() )
        {
            if ( user.getUserId().equals( userId ) )
            {
                return user;
            }
        }
        throw new UserNotFoundException( userId );
    }

}
