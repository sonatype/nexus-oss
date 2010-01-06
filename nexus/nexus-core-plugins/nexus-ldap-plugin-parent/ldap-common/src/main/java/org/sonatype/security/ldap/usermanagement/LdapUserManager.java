/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.usermanagement;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.UserStatus;

@Component( role = UserManager.class, hint = "LDAP" )
public class LdapUserManager
    extends AbstractReadOnlyUserManager
{

    public static final String LDAP_REALM_KEY = "LdapAuthenticatingRealm";
    private static final String USER_SOURCE = "LDAP";

    @Requirement
    private LdapManager ldapManager;

    @Requirement
    private Logger logger;

    public User getUser( String userId )
    {
        if ( this.isEnabled() )
        {
            try
            {
                return toPlexusUser( this.ldapManager.getUser( userId ) );
            }
            catch ( NoSuchLdapUserException e )
            {
                this.logger.debug( "User: " + userId + " not found.", e );
            }
            catch ( LdapDAOException e )
            {
                this.logger.debug( "User: " + userId + " not found, cause: " + e.getMessage(), e );
            }
        }
        return null;
    }


    public Set<String> listUserIds()
    {
        Set<String> userIds = new TreeSet<String>();
        for ( User User : this.listUsers() )
        {
            userIds.add( User.getUserId() );
        }
        return userIds;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new TreeSet<User>();
        if ( this.isEnabled() )
        {
            try
            {
                Collection<LdapUser> ldapUsers = this.ldapManager.getAllUsers();
                for ( LdapUser ldapUser : ldapUsers )
                {
                    users.add( this.toPlexusUser( ldapUser ) );
                }
            }
            catch ( LdapDAOException e )
            {
                this.logger.debug( "Could not return LDAP users, LDAP Realm must not be configured.", e );
            }
        }
        return users;
    }

    private User toPlexusUser( LdapUser ldapUser )
    {
        User user = new DefaultUser();
        user.setEmailAddress( ldapUser.getEmail() );
        user.setName( ldapUser.getRealName() );
        user.setUserId( ldapUser.getUsername() );
        user.setSource( USER_SOURCE );
        user.setStatus( UserStatus.active );
        

        for ( String roleId : ldapUser.getMembership() )
        {
            RoleIdentifier role = new RoleIdentifier( USER_SOURCE, roleId );
            user.addRole( role );
        }

        return user;
    }

    private boolean isEnabled()
    {
        return true;
//        return this.securitySystem.getRealms().contains( "LDAP" );
    }

    public String getSource()
    {
        return USER_SOURCE;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        //TODO, rename method, we are doing a starts with search, but thats not what this signature implies,
        // but I don't have a better idea right now.

        Set<User> users = new TreeSet<User>();
        if ( this.isEnabled() )
        {
            try
            {
                Set<LdapUser> ldapUsers = this.ldapManager.searchUsers( criteria.getUserId() );

                for ( LdapUser ldapUser : ldapUsers )
                {
                    users.add( this.toPlexusUser( ldapUser ) );
                }
            }
            catch ( LdapDAOException e )
            {
                this.logger.debug( "Could not return LDAP users, LDAP Realm must not be configured.", e );
            }
        }

        // we can filter the lists in memory to weed out the non effective users
        // we can not *easily* do this with a LDAP query.  It would be easy for
        // users with dynamic groups, but not static
        return this.filterListInMemeory( users, criteria );
    }


    public String getAuthenticationRealmName()
    {
        return LDAP_REALM_KEY;
    }

}
