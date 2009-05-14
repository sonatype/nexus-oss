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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.NoSuchUserManager;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

import edu.emory.mathcs.backport.java.util.Collections;

@Component( role = UserManager.class, hint = "allConfigured", description = "All Configured Users" )
public class ConfiguredUsersUserManager
    extends AbstractReadOnlyUserManager
{

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer container;
    
    // @Requirement
    private SecuritySystem securitySystem;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public static final String SOURCE = "allConfigured";

    public String getSource()
    {
        return SOURCE;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            try
            {
                User user = this.getSecuritySystem().getUser( userRoleMapping.getUserId(), userRoleMapping.getSource() );
                if ( user != null )
                {
                    users.add( user );
                }
            }
            catch ( UserNotFoundException e )
            {
                this.logger.warn( "User: '" + userRoleMapping.getUserId() + "' of source: '"
                    + userRoleMapping.getSource() + "' could not be found.", e );
            }
            catch ( NoSuchUserManager e )
            {
                this.logger.warn( "User: '" + userRoleMapping.getUserId() + "' of source: '"
                    + userRoleMapping.getSource() + "' could not be found.", e );
            }
        }

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        Set<User> users = new HashSet<User>();

        for ( User user : users )
        {
            userIds.add( user.getUserId() );
        }

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            String userId = userRoleMapping.getUserId();
            if ( StringUtils.isNotEmpty( userId ) )
            {
                userIds.add( userId );
            }
        }

        return userIds;
    }

    public User getUser( String userId )
    {
        // this resource will only list the users
        return null;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        // we only want to do this if the criteria is set to the source
        if ( this.getSource().equals( criteria.getSource() ))
        {
            return this.filterListInMemeory( this.listUsers(), criteria );
        }
        else
        {
            return new HashSet<User>();
        }
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


    
    /* (non-Javadoc)
     * @see org.sonatype.security.usermanagement.AbstractUserManager#matchesCriteria(java.lang.String, java.lang.String, java.util.Collection, org.sonatype.security.usermanagement.UserSearchCriteria)
     */
    protected boolean matchesCriteria( String userId, String userSource, Collection<String> usersRoles,
        UserSearchCriteria criteria )
    {
        // basically the same as the super, but we don't want to check the source
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !userId.toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }

        if ( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            if ( usersRoles != null )
            {
                userRoles.addAll( usersRoles );
            }

            // check the intersection of the roles
            if ( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty() )
            {
                return false;
            }
        }

        return true;
    }
    
}
