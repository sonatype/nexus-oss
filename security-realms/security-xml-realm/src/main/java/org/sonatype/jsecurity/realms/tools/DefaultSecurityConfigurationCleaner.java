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
package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.model.Configuration;

@Component( role = SecurityConfigurationCleaner.class )
public class DefaultSecurityConfigurationCleaner
    extends AbstractLogEnabled
    implements SecurityConfigurationCleaner
{    
    public void privilegeRemoved( Configuration configuration, String privilegeId )
    {
        getLogger().debug( "Cleaning privilege id " + privilegeId + " from roles." );
        List<CRole> roles = configuration.getRoles();
        
        for ( CRole role : roles )
        {
            if ( role.getPrivileges().contains( privilegeId ) )
            {
                getLogger().debug( "removing from role " + role.getId() );
                role.getPrivileges().remove( privilegeId );
            }
        }
    }

    public void roleRemoved( Configuration configuration, String roleId )
    {
        getLogger().debug( "Cleaning role id " + roleId + " from users and roles." );
        List<CRole> roles = configuration.getRoles();
        
        for ( CRole role : roles )
        {
            if ( role.getRoles().contains( roleId ) )
            {
                getLogger().debug( "removing from role " + role.getId() );
                role.getRoles().remove( roleId );
            }
        }
        
        List<CUserRoleMapping> mappings = configuration.getUserRoleMappings();
        
        for ( CUserRoleMapping mapping : mappings )
        {
            if ( mapping.getRoles().contains( roleId ) )
            {
                getLogger().debug( "removing from user " + mapping.getUserId() );
                mapping.removeRole( roleId );
            }
        }
    }
}
