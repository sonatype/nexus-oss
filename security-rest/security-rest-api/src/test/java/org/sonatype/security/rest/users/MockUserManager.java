/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.rest.users;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

public class MockUserManager extends AbstractReadOnlyUserManager
{
    public String getSource()
    {
        return "MockUserManager";
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        User a = new DefaultUser();
        a.setName( "Joe Coder" );
        a.setEmailAddress( "jcoder@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "jcoder" );
        a.setStatus( UserStatus.active );
        a.addRole( this.createFakeRole( "Role1" ) );
        a.addRole( this.createFakeRole( "Role2" ) );
        a.addRole( this.createFakeRole( "Role3" ) );

        User b = new DefaultUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.setStatus( UserStatus.active );
        b.addRole( this.createFakeRole( "Role2" ) );
        b.addRole( this.createFakeRole( "Role3" ) );

        User c = new DefaultUser();
        c.setName( "Patricia P. Peralez" );
        c.setEmailAddress( "pperalez@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "pperalez" );
        c.setStatus( UserStatus.active );
        c.addRole( this.createFakeRole( "Role1" ) );
        c.addRole( this.createFakeRole( "Role2" ) );

        User d = new DefaultUser();
        d.setName( "Danille S. Knudsen" );
        d.setEmailAddress( "dknudsen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "dknudsen" );
        d.setStatus( UserStatus.active );
        d.addRole( this.createFakeRole( "Role4" ) );
        d.addRole( this.createFakeRole( "Role2" ) );

        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );

        return users;
    }

    public boolean isPrimary()
    {
        return true;
    }

    public User getUser( String userId )
    {
        Set<User> users = this.listUsers();

        for ( User User : users )
        {
            if ( User.getUserId().equals( userId ) )
            {
                return User;
            }
        }

        return null;
    }

    public Set<String> listUserIds()
    {
        Set<String> result = new HashSet<String>();
        for ( User User : this.listUsers() )
        {
            result.add( User.getUserId() );
        }
        return result;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {

        Set<User> result = new HashSet<User>();
        for ( User User : this.listUsers() )
        {
            if ( User.getUserId().toLowerCase().startsWith( criteria.getUserId() ) );
            {
                result.add( User );
            }
        }
        return result;
    }
    protected RoleIdentifier createFakeRole( String roleId )
    {
        RoleIdentifier role = new RoleIdentifier( this.getSource(), roleId );
        return role;
    }

    public String getAuthenticationRealmName()
    {
        return null;
    }

}
