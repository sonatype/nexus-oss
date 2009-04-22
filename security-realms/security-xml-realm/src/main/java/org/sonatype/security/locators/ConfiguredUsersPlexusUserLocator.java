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
package org.sonatype.security.locators;

import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserManager;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;
import org.sonatype.security.locators.users.UserManager;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;

@Component( role = UserManager.class, hint = "allConfigured", description = "All Configured Users" )
public class ConfiguredUsersPlexusUserLocator
    extends AbstractPlexusUserLocator
{

    @Requirement( hint = "additinalRoles" )
    private PlexusUserManager userManager;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public static final String SOURCE = "allConfigured";
    
    public String getSource()
    {
        return SOURCE;
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = this.userManager.listUsers( SecurityXmlPlexusUserLocator.SOURCE );

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( !SecurityXmlPlexusUserLocator.SOURCE.equals( userRoleMapping.getSource() ) )
            {
                PlexusUser user = this.userManager.getUser( userRoleMapping.getUserId(), userRoleMapping.getSource() );
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
        Set<String> userIds = this.userManager.listUserIds( SecurityXmlPlexusUserLocator.SOURCE );

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( !SecurityXmlPlexusUserLocator.SOURCE.equals( userRoleMapping.getSource() ) )
            {
                String userId = userRoleMapping.getUserId();
                if ( StringUtils.isNotEmpty( userId ))
                {
                    userIds.add( userId );
                }
            }
        }
        
        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        // this resource will only list the users
        return null;
    }
    
    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria )
    {
        return this.filterListInMemeory( this.listUsers(), criteria );
    }

    public boolean isPrimary()
    {
        return false;
    }

}
