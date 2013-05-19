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
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.inject.Description;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.url.config.UrlRealmConfiguration;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;
import org.sonatype.security.usermanagement.xml.ConfiguredUsersUserManager;

/**
 * The UserManager for the URL Realm. The remote URL used by the URL Realm is NOT hit. When performing a search for a
 * user the name in the search criteria is returned. <BR/>
 * NOTE: This realm is typically used when trying to integrate with an existing system, and another directory is not
 * available.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( UserManager.class )
@Named( "url" )
@Description( "URL Realm Users" )
public class URLUserManager
    extends AbstractReadOnlyUserManager
{
    public static final String SOURCE = "url";

    private final ConfigurationManager configuration;

    private final UrlRealmConfiguration urlRealmConfiguration;

    private final List<UserManager> userLocators;

    @Inject
    public URLUserManager( @Named( "default" ) ConfigurationManager configuration,
                           List<UserManager> userLocators, UrlRealmConfiguration urlRealmConfiguration )
    {
        this.configuration = configuration;
        this.userLocators = userLocators;
        this.urlRealmConfiguration = urlRealmConfiguration;
    }

    public String getSource()
    {
        return SOURCE;
    }

    public User getUser( String userId )
    {
        // list of users to ignore
        Set<String> ignoredUsers = this.getIgnoredUserIds();

        if ( ignoredUsers.contains( userId ) )
        {
            return null;
        }

        // otherwise search for the user, the search will fake the user if its not in the security.xml
        Set<User> users = this.searchUsers( new UserSearchCriteria( userId ) );

        // now find the user
        for ( User user : users )
        {
            if ( user.getUserId().equals( userId ) )
            {
                return user;
            }
        }

        return null;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        List<CUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();

        for ( CUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( SOURCE.equals( userRoleMapping.getSource() ) )
            {
                User user = null;

                if ( userRoleMapping.getRoles().contains( this.urlRealmConfiguration.getConfiguration().getDefaultRole() ) )
                {
                    user = this.toUser( userRoleMapping.getUserId(), false );
                }
                else
                {
                    user = this.toUser( userRoleMapping.getUserId(), true );
                }

                if ( user != null )
                {
                    users.add( user );
                }
            }
        }

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        List<CUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( CUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( SOURCE.equals( userRoleMapping.getSource() ) )
            {
                String userId = userRoleMapping.getUserId();
                if ( StringUtils.isNotEmpty( userId ) )
                {
                    userIds.add( userId );
                }
            }
        }

        return userIds;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        Set<User> result = new HashSet<User>();

        if ( StringUtils.isNotEmpty( criteria.getUserId() ) )
        {
            String userId = criteria.getUserId();

            // first we need to check if we need to ignore one of these users
            if ( this.getIgnoredUserIds().contains( userId ) )
            {
                return result;
            }

            for ( User user : this.listUsers() )
            {
                if ( user.getUserId().toLowerCase().startsWith( userId.toLowerCase() ) )
                {
                    result.add( user );
                }
            }

            // this is a bit fuzzy, because we want to return a user even if we didn't find one
            // first check if we had an exact match

            User exactUser = null;
            for ( User user : result )
            {
                if ( user.getUserId().toLowerCase().equals( userId.toLowerCase() ) )
                {
                    exactUser = user;
                }
            }
            // if not exact user is found, fake it
            if ( exactUser == null )
            {
                result.add( this.toUser( userId, true ) );
            }
        }
        else
        {
            result = this.listUsers();
        }

        // this will the on things other then the userId
        return this.filterListInMemeory( result, criteria );
    }

    private User toUser( String userId, boolean addDefaultRole )
    {
        String defaultRole = urlRealmConfiguration.getConfiguration().getDefaultRole();
        String emailDomain = urlRealmConfiguration.getConfiguration().getEmailDomain();

        DefaultUser user = new DefaultUser();
        user.setEmailAddress( userId + "@" + emailDomain );
        user.setName( userId );
        user.setSource( SOURCE );
        user.setUserId( userId );

        if ( addDefaultRole )
        {
            user.addRole( new RoleIdentifier( SOURCE, defaultRole ) );
        }

        return user;
    }

    private Set<String> getIgnoredUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( UserManager userLocator : this.userLocators )
        {
            if ( !this.getSource().equals( userLocator.getSource() )
                && !ConfiguredUsersUserManager.SOURCE.equals( userLocator.getSource() ) )
            {
                userIds.addAll( userLocator.listUserIds() );
            }
        }

        return userIds;
    }

    public String getAuthenticationRealmName()
    {
        return "url";
    }
}
