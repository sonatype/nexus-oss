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
@Named( value = "Mock" )
@Typed( value = UserManager.class )
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
