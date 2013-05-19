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
package org.sonatype.security.realms.tools;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUserRoleMapping;

/**
 * Removes dead references to roles and permissions in the security model. When a permission is removed all roles will
 * be updated so the permission reference can removed. When a Role is removed references are removed from other roles
 * and users.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( SecurityConfigurationCleaner.class )
@Named( "default" )
public class DefaultSecurityConfigurationCleaner
    implements SecurityConfigurationCleaner
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public void privilegeRemoved( EnhancedConfiguration configuration, String privilegeId )
    {
        logger.debug( "Cleaning privilege id {} from roles.", privilegeId );
        List<CRole> roles = configuration.getRoles();

        for ( CRole role : roles )
        {
            if ( role.getPrivileges().contains( privilegeId ) )
            {
                logger.debug( "removing privilege {} from role {}", privilegeId, role.getId() );
                role.getPrivileges().remove( privilegeId );
                configuration.removeRoleById( role.getId() );
                configuration.addRole( role );
            }
        }
    }

    public void roleRemoved( EnhancedConfiguration configuration, String roleId )
    {
        logger.debug( "Cleaning role id {} from users and roles.", roleId );
        List<CRole> roles = configuration.getRoles();

        for ( CRole role : roles )
        {
            if ( role.getRoles().contains( roleId ) )
            {
                logger.debug( "removing ref to role {} from role {}", roleId, role.getId() );
                role.getRoles().remove( roleId );
                configuration.removeRoleById( role.getId() );
                configuration.addRole( role );
            }
        }

        List<CUserRoleMapping> mappings = configuration.getUserRoleMappings();

        for ( CUserRoleMapping mapping : mappings )
        {
            if ( mapping.getRoles().contains( roleId ) )
            {
                logger.debug( "removing ref to role {} from user {}", mapping.getUserId() );
                mapping.removeRole( roleId );
                configuration.removeUserRoleMappingByUserId( mapping.getUserId(), mapping.getSource() );
                configuration.addUserRoleMapping( mapping );
            }
        }
    }
}
