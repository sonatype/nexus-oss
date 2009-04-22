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
package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.RoleResolver;
import org.sonatype.jsecurity.locators.SecurityXmlPlexusUserLocator;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.locators.users.DefaultPlexusUserManager;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserManager;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;

@Component( role = PlexusUserManager.class, hint = "additinalRoles" )
public class AdditinalRolePlexusUserManager
    extends DefaultPlexusUserManager
{
    @Requirement( hint = "resourceMerging" )
    private ConfigurationManager configManager;
    
    @Requirement
    private Logger logger;
    
    @Requirement
    private RoleResolver roleResolver;

    @Override
    public PlexusUser getUser( String userId )
    {
        PlexusUser user = super.getUser( userId );

        if ( user != null )
        {
            this.populateAdditionalRoles( user );
        }

        return user;
    }

    @Override
    public PlexusUser getUser( String userId, String source )
    {
        PlexusUser user = super.getUser( userId, source );

        if ( user != null )
        {
            this.populateAdditionalRoles( user );
        }

        return user;
    }

    @Override
    public Set<PlexusUser> listUsers( String source )
    {
        Set<PlexusUser> users = super.listUsers( source );
        // add the roles mapped in the security.xml
        this.populateAdditionalRoles( users );

        return users;
    }

    @Override
    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria, String source )
    {
        // we need to resolve the nested roles TODO: should we be changing the criteria object?
        // the security.xml file allows nested roles.
        criteria.setOneOfRoleIds( roleResolver.effectiveRoles( criteria.getOneOfRoleIds() ) );
        
        Set<PlexusUser> users = super.searchUsers( criteria, source );

        // add the roles mapped in the security.xml
        this.populateAdditionalRoles( users );

        // if the source is not ALL, we need to search users mapped in the secuirty.xml
        // we could just search the ConfiguredUsersPlexusUserLocator, but that could could
        // make for unneeded queries to external Realms, so we are going to filter on the SecurityUserRoleMapping.
        // NOTE: these users will already have the additionalRoles populated.
        users.addAll( this.searchMappedUsers( criteria, source ) );

        return users;
    }

    private Set<PlexusUser> searchMappedUsers( PlexusUserSearchCriteria criteria, String source )
    {
        Set<PlexusUser> mappedUsers = new HashSet<PlexusUser>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configManager.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( this.userMatchesCriteria( userRoleMapping, criteria, source ) )
            {
                PlexusUser user =  this.getUser( userRoleMapping.getUserId(), userRoleMapping.getSource() );
                if( user != null )
                {
                    mappedUsers.add( user );
                }
            }
        }
        return mappedUsers;
    }

    private boolean userMatchesCriteria( SecurityUserRoleMapping roleMapping, PlexusUserSearchCriteria criteria,
        String source )
    {
        // yeah i know its multiple returns, but its really easy to debug this way (and thats more important)

        if ( !roleMapping.getSource().equals( source ) )
        {
            return false;
        }

        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !criteria.getUserId().toLowerCase().startsWith( roleMapping.getUserId().toLowerCase() ) )
        {
            return false;
        }

        if ( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            // check the intersection of the roles
            if ( CollectionUtils.intersection( criteria.getOneOfRoleIds(), roleMapping.getRoles() ).isEmpty() )
            {
                return false;
            }
        }

        return true;
    }

    private void populateAdditionalRoles( Set<PlexusUser> users )
    {
        for ( PlexusUser user : users )
        {
            this.populateAdditionalRoles( user );
        }
    }

    @SuppressWarnings( "unchecked" )
    private void populateAdditionalRoles( PlexusUser user )
    {
        try
        {
            // this throws a NoSuchRoleMappingException (it is ok)
            CUserRoleMapping roleMapping = configManager.readUserRoleMapping( user.getUserId(), user.getSource() );

            for ( String roleId : (List<String>) roleMapping.getRoles() )
            {
                try
                {
                    if ( configManager.readRole( roleId ) != null ) // this will throw an exception if the role does not
                                                                    // exist, but we are going to check anyway
                    {
                        user.getRoles().add( this.toPlexusRole( roleId ) );
                    }
                }
                catch ( NoSuchRoleException e )
                {
                    this.logger.error( "User: '" + user.getUserId() + "' from source: '" + user.getSource()
                        + "' has role: '" + roleId + "' but the role could not be found." );
                }
            }
        }
        catch ( NoSuchRoleMappingException e )
        {
            // this is ok, it will happen most of the time
        }
    }

    // FIXME duplicate code
    protected PlexusRole toPlexusRole( String roleId )
    {
        if ( roleId == null )
        {
            return null;
        }

        try
        {
            CRole role = configManager.readRole( roleId );

            PlexusRole plexusRole = new PlexusRole();

            plexusRole.setRoleId( role.getId() );
            plexusRole.setName( role.getName() );
            plexusRole.setSource( SecurityXmlPlexusUserLocator.SOURCE );

            return plexusRole;
        }
        catch ( NoSuchRoleException e )
        {
            return null;
        }
    }
}
