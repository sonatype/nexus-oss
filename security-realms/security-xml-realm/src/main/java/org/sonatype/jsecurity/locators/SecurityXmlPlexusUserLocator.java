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
package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;
import org.sonatype.security.locators.users.UserManager;

@Component( role = UserManager.class, description = "Default" )
public class SecurityXmlPlexusUserLocator
    extends AbstractPlexusUserLocator
{
    public static final String SOURCE = "default";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement
    private Logger logger;

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        for ( CUser user : configuration.listUsers() )
        {
            users.add( toPlexusUser( user ) );
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

    public PlexusUser getUser( String userId )
    {
        try
        {
            PlexusUser user = toPlexusUser( configuration.readUser( userId ) );
            return user;
        }
        catch ( NoSuchUserException e )
        {
            return null;
        }
    }

    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    public boolean isPrimary()
    {
        // This locator will never be primary, if left standalone will
        // act as primary, otherwise other locators will be treated as primary
        return false;
    }

    protected PlexusUser toPlexusUser( CUser user )
    {
        if ( user == null )
        {
            return null;
        }

        PlexusUser plexusUser = new PlexusUser();

        plexusUser.setUserId( user.getId() );
        plexusUser.setName( user.getName() );
        plexusUser.setEmailAddress( user.getEmail() );
        plexusUser.setSource( SOURCE );

        CUserRoleMapping roleMapping;
        try
        {
            roleMapping = this.configuration.readUserRoleMapping( user.getId(), SOURCE );

            if ( roleMapping != null )
            {
                for ( String role : (List<String>) roleMapping.getRoles() )
                {
                    PlexusRole plexusRole = toPlexusRole( role );
                    if ( plexusRole != null )
                    {
                        plexusUser.addRole( plexusRole );
                    }
                }
            }
        }
        catch ( NoSuchRoleMappingException e )
        {
            this.logger.debug( "No user role mapping found for user: " + user.getId() );

        }

        return plexusUser;
    }

    protected PlexusRole toPlexusRole( String roleId )
    {
        if ( roleId == null )
        {
            return null;
        }

        try
        {
            CRole role = configuration.readRole( roleId );

            PlexusRole plexusRole = new PlexusRole();

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
}
