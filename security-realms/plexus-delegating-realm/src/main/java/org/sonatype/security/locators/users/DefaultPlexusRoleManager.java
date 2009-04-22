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
package org.sonatype.security.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = PlexusRoleManager.class )
public class DefaultPlexusRoleManager
    implements PlexusRoleManager
{

    public static final String SOURCE_ALL = "all";

    @Requirement( role = PlexusRoleLocator.class )
    private List<PlexusRoleLocator> locators;

    public Set<String> listRoleIds( String source )
    {
        Set<String> roles = new TreeSet<String>();

        for ( PlexusRoleLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                Set<String> locatorRoles = locator.listRoleIds();
                if( locatorRoles != null )
                {
                    roles.addAll( locatorRoles );
                }
            }
        }

        return roles;
    }

    public Set<PlexusRole> listRoles( String source )
    {
        Set<PlexusRole> roles = new TreeSet<PlexusRole>();

        for ( PlexusRoleLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                Set<PlexusRole> locatorRoles = locator.listRoles();
                if( locatorRoles != null )
                {
                    roles.addAll( locatorRoles );
                }
            }
        }

        return roles;
    }

}
