/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
