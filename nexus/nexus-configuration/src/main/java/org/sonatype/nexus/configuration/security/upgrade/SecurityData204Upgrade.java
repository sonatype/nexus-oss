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
package org.sonatype.nexus.configuration.security.upgrade;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.upgrade.AbstractDataUpgrader;
import org.sonatype.security.model.upgrade.SecurityDataUpgrader;

@Singleton
@Typed( value = SecurityDataUpgrader.class )
@Named( value = "2.0.4" )
public class SecurityData204Upgrade
    extends AbstractDataUpgrader<Configuration>
    implements SecurityDataUpgrader
{

    private static final String[] DEPRECATED_ROLES = new String[] { "admin", "deployment", "developer" };

    @Override
    public void doUpgrade( Configuration cfg )
        throws ConfigurationIsCorruptedException
    {
        for ( CRole role : cfg.getRoles() )
        {
            updateDeprecatedRoles( role.getRoles() );
        }

        for ( CUserRoleMapping map : cfg.getUserRoleMappings() )
        {
            updateDeprecatedRoles( map.getRoles() );
        }
    }

    private void updateDeprecatedRoles( List<String> roles )
    {
        for ( String role : DEPRECATED_ROLES )
        {
            if ( roles.contains( role ) )
            {
                roles.remove( role );
                roles.add( "nx-" + role );
            }
        }

    }

}
