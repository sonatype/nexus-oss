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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusRoleLocator;

/**
 * PlexusRoleLocator that wraps roles from security-xml-realm.
 */
@Component( role = PlexusRoleLocator.class )
public class SecurityXmlPlexusRoleLocator
    implements PlexusRoleLocator
{

    public static final String SOURCE = "default";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public String getSource()
    {
        return SOURCE;
    }

    public Set<String> listRoleIds()
    {
        Set<String> roleIds = new TreeSet<String>();
        List<SecurityRole> secRoles = this.configuration.listRoles();

        for ( SecurityRole securityRole : secRoles )
        {
            roleIds.add( securityRole.getId() );
        }

        return roleIds;
    }

    public Set<PlexusRole> listRoles()
    {
        Set<PlexusRole> roles = new TreeSet<PlexusRole>();
        List<SecurityRole> secRoles = this.configuration.listRoles();

        for ( SecurityRole securityRole : secRoles )
        {
            roles.add( this.toPlexusRole( securityRole ) );
        }

        return roles;
    }

    protected PlexusRole toPlexusRole( SecurityRole role )
    {

        PlexusRole plexusRole = new PlexusRole();

        plexusRole.setRoleId( role.getId() );
        plexusRole.setName( role.getName() );
        plexusRole.setSource( SOURCE );

        return plexusRole;

    }

}
