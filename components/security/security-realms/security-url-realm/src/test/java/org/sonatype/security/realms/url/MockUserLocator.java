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
package org.sonatype.security.realms.url;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

@Singleton
@Typed( UserManager.class )
@Named( "test" )
public class MockUserLocator
    extends AbstractReadOnlyUserManager
{
    private Set<String> userIds = new HashSet<String>();

    public MockUserLocator()
    {
        userIds.add( "bob" );
        userIds.add( "jcoder" );
    }

    public String getSource()
    {
        return "test";
    }

    public User getUser( String userId )
    {
        if ( this.userIds.contains( userId ) )
        {
            return this.toUser( userId );
        }
        return null;
    }

    public boolean isPrimary()
    {
        return false;
    }

    public Set<String> listUserIds()
    {
        return userIds;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        for ( String userId : this.userIds )
        {
            users.add( this.toUser( userId ) );
        }

        return users;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    private User toUser( String userId )
    {
        DefaultUser user = new DefaultUser();

        user.setUserId( userId );
        user.setName( userId );
        user.setEmailAddress( userId + "@foo.com" );
        user.setSource( this.getSource() );

        return user;
    }

    public String getAuthenticationRealmName()
    {
        return null;
    }
}
