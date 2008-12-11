/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.jsecurity.realms;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = Realm.class, hint = "NexusTargetAuthorizingRealm" )
public class NexusTargetAuthorizingRealm
    extends AbstractNexusAuthorizingRealm
{
    public static final String PRIVILEGE_TYPE_TARGET = "target";

    public static final String PRIVILEGE_PROPERTY_REPOSITORY_TARGET = "repositoryTargetId";

    public static final String PRIVILEGE_PROPERTY_REPOSITORY_ID = "repositoryId";

    public static final String PRIVILEGE_PROPERTY_REPOSITORY_GROUP_ID = "repositoryGroupId";

    @Requirement
    private Nexus nexus;

    @Override
    protected Set<Permission> getPermissions( String privilegeId )
    {
        try
        {
            SecurityPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );

            if ( !privilege.getType().equals( PRIVILEGE_TYPE_TARGET ) )
            {
                return Collections.emptySet();
            }

            String repositoryTarget = getConfigurationManager().getPrivilegeProperty(
                privilege,
                PRIVILEGE_PROPERTY_REPOSITORY_TARGET );
            String method = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_METHOD );
            String repositoryId = getConfigurationManager().getPrivilegeProperty(
                privilege,
                PRIVILEGE_PROPERTY_REPOSITORY_ID );
            String repositoryGroupId = getConfigurationManager().getPrivilegeProperty(
                privilege,
                PRIVILEGE_PROPERTY_REPOSITORY_GROUP_ID );

            StringBuilder basePermString = new StringBuilder();

            basePermString.append( "nexus:target:" );
            basePermString.append( repositoryTarget );
            basePermString.append( ":" );

            StringBuilder postPermString = new StringBuilder();

            postPermString.append( ":" );

            if ( StringUtils.isEmpty( method ) )
            {
                postPermString.append( "*" );
            }
            else
            {
                postPermString.append( method );
            }

            if ( !StringUtils.isEmpty( repositoryId ) )
            {
                return Collections.singleton( (Permission) new WildcardPermission( basePermString + repositoryId
                    + postPermString ) );
            }
            else if ( !StringUtils.isEmpty( repositoryGroupId ) )
            {
                try
                {
                    Set<Permission> permissions = new HashSet<Permission>();

                    List<Repository> repositories = nexus.getRepositoryGroup( repositoryGroupId );

                    for ( Repository repository : repositories )
                    {
                        WildcardPermission permission = new WildcardPermission( basePermString + repository.getId()
                            + postPermString );

                        permissions.add( permission );
                    }

                    return permissions;
                }
                catch ( NoSuchRepositoryGroupException e )
                {
                    // If there is no such group you don't have permission to it
                    return Collections.emptySet();
                }
            }
            else
            {
                return Collections.singleton( (Permission) new WildcardPermission( basePermString + "*"
                    + postPermString ) );
            }
        }
        catch ( NoSuchPrivilegeException e1 )
        {
            return Collections.emptySet();
        }
    }
}
