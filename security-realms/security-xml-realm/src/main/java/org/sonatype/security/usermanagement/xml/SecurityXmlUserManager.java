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
package org.sonatype.security.usermanagement.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.usermanagement.AbstractUserManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

@Component( role = UserManager.class, description = "Default" )
public class SecurityXmlUserManager
    extends AbstractUserManager
{
    public static final String SOURCE = "default";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement
    private PlexusContainer container;

    private SecuritySystem securitySystem;

    @Requirement
    private Logger logger;

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        for ( CUser user : configuration.listUsers() )
        {
            users.add( toUser( user ) );
        }

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( CUser user : configuration.listUsers() )
        {
            userIds.add( user.getId() );
        }

        return userIds;
    }

    public User getUser( String userId )
        throws UserNotFoundException
    {
        User user = toUser( configuration.readUser( userId ) );
        return user;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        Set<User> users = new HashSet<User>();
        users.addAll( this.filterListInMemeory( this.listUsers(), criteria ) );

        // we also need to search through the user role mappings.

        List<SecurityUserRoleMapping> roleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping roleMapping : roleMappings )
        {
            if ( !SOURCE.equals( roleMapping.getSource() ) )
            {
                if ( this.matchesCriteria(
                    roleMapping.getUserId(),
                    roleMapping.getSource(),
                    roleMapping.getRoles(),
                    criteria ) )
                {
                    try
                    {
                        User user = this.getSecuritySystem().getUser( roleMapping.getUserId(), roleMapping.getSource() );
                        users.add( user );
                    }
                    catch ( UserNotFoundException e )
                    {
                        this.logger.warn( "User: '" + roleMapping.getUserId() + "' of source: '"
                            + roleMapping.getSource() + "' could not be found.", e );
                    }
                    catch ( NoSuchUserManager e )
                    {
                        this.logger.warn( "User: '" + roleMapping.getUserId() + "' of source: '"
                            + roleMapping.getSource() + "' could not be found.", e );
                    }

                }
            }
        }

        return users;
    }

    protected User toUser( CUser cUser )
    {
        if ( cUser == null )
        {
            return null;
        }

        DefaultUser user = new DefaultUser();

        user.setUserId( cUser.getId() );
        user.setName( cUser.getName() );
        user.setEmailAddress( cUser.getEmail() );
        user.setSource( SOURCE );

        try
        {
            user.setRoles( this.getUsersRoles( cUser.getId(), SOURCE ) );
        }
        catch ( UserNotFoundException e )
        {
            // We should NEVER get here
            this.logger.warn( "Could not find user: '" + cUser.getId() + "' of source: '" + SOURCE
                + "' while looking up the users roles.", e );
        }

        return user;
    }

    protected Role toRole( String roleId )
    {
        if ( roleId == null )
        {
            return null;
        }

        try
        {
            CRole role = configuration.readRole( roleId );

            Role plexusRole = new Role();

            plexusRole.setRoleId( role.getId() );
            plexusRole.setName( role.getName() );
            plexusRole.setSource( SOURCE );

            return plexusRole;
        }
        catch ( NoSuchRoleException e )
        {
            return null;
        }
    }

    public String getSource()
    {
        return SOURCE;
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

    public User updateUser( User user )
        throws UserNotFoundException
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
        Set<Role> roles = new HashSet<Role>();

        CUserRoleMapping roleMapping;
        try
        {
            roleMapping = this.configuration.readUserRoleMapping( userId, source );

            if ( roleMapping != null )
            {
                for ( String roleId : (List<String>) roleMapping.getRoles() )
                {
                    Role role = toRole( roleId );
                    if ( role != null )
                    {
                        roles.add( role );
                    }
                }
            }
        }
        catch ( NoSuchRoleMappingException e )
        {
            this.logger.debug( "No user role mapping found for user: " + userId );
        }
        return roles;
    }

    public void setUsersRoles( String userId, Set<Role> roles, String source )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub

    }

    private SecuritySystem getSecuritySystem()
    {
        // FIXME: hack, we need to lazy load the security system, due to a circular dependency
        if ( this.securitySystem == null )
        {
            try
            {
                this.securitySystem = this.container.lookup( SecuritySystem.class );
            }
            catch ( ComponentLookupException e )
            {
                this.logger.error( "Unable to load SecuritySystem", e );
            }
        }

        return this.securitySystem;
    }
}
