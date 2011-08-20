/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.migration.nexus1434;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.hamcrest.Matchers;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;

public class Nexus1434ImportSecurity130DisablePermissionIT
    extends AbstractImportSecurityIT
{

    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-130.zip" ) );

        migrationSummary.setResolvePermission( false );

        commitMigration( migrationSummary );
    }

    @Override
    protected void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeStatusResource> privilegeList = getImportedTargetPrivilegesList();
        List<RoleResource> roleList = getImportedRoleList();

        assertThat( "4 users imported", userList.size(), is( equalTo( 4 ) ) );
        assertThat( targetList, is( Matchers.<RepositoryTargetListResource> empty() ) );
        assertThat( privilegeList, is( Matchers.<PrivilegeStatusResource> empty() ) );
        // the group is coverted to a role now
        assertThat( roleList.size(), is( equalTo( 1 ) ) );

        // these users are imported
        assertThat( "User list contains 'anonymous-artifactory'", containUser( userList, "anonymous-artifactory" ) );
        assertThat( "User list contains 'admin-artifactory'", containUser( userList, "admin-artifactory" ) );
        assertThat( "User list contains 'user'", containUser( userList, "user" ) );
        assertThat( "User list contains 'user1'", containUser( userList, "user1" ) );

        // verify user-role mapping
        PlexusUserResource anonymous = getUserById( userList, "anonymous-artifactory" );
        assertThat( anonymous.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( anonymous.getRoles(), "anonymous" );

        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        assertThat( admin.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( admin.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        assertThat( user.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( user.getRoles(), "group" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        assertThat( user1.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( user1.getRoles(), "anonymous" );

        // verify the group role
        RoleResource groupRole = getRoleById( roleList, "group" );
        assertThat( groupRole, is( notNullValue() ) );
        assertThat( groupRole.getRoles(), hasItem( "anonymous" ) );
    }

}
