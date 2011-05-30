/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
