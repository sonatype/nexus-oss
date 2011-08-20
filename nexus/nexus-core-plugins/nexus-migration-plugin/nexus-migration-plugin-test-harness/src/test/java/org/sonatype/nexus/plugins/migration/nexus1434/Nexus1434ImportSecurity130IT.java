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

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;

public class Nexus1434ImportSecurity130IT
    extends AbstractImportSecurityIT
{

    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-130.zip" ) );

        migrationSummary.setResolvePermission( true );

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
        assertThat( "3 repo targets imported", targetList.size(), is( equalTo( 3 ) ) );
        assertThat( "4 privileges for each repo target", privilegeList.size(), is( equalTo( targetList.size() * 4 ) ) );
        assertThat( "4 roles for each repo target, plus a group", roleList.size(),
            is( equalTo( targetList.size() * 4 + 1 ) ) );

        // these users are imported
        assertThat( "User list contains 'anonymous-artifactory'", containUser( userList, "anonymous-artifactory" ) );
        assertThat( "User list contains 'admin-artifactory'", containUser( userList, "admin-artifactory" ) );
        assertThat( "User list contains 'user'", containUser( userList, "user" ) );
        assertThat( "User list contains 'user1'", containUser( userList, "user1" ) );

        assertThat( "Contains privilege Anything-ANY-create",
            containPrivilegeName( privilegeList, "Anything-ANY-create" ) );
        assertThat( "Contains privilege Anything-ANY-read", containPrivilegeName( privilegeList, "Anything-ANY-read" ) );
        assertThat( "Contains privilege Anything-ANY-update",
            containPrivilegeName( privilegeList, "Anything-ANY-update" ) );
        assertThat( "Contains privilege Anything-ANY-delete",
            containPrivilegeName( privilegeList, "Anything-ANY-delete" ) );

        assertThat( "Contains role Anything-ANY-reader", containRole( roleList, "Anything-ANY-reader" ) );
        assertThat( "Contains role Anything-ANY-deployer", containRole( roleList, "Anything-ANY-deployer" ) );
        assertThat( "Contains role Anything-ANY-delete", containRole( roleList, "Anything-ANY-delete" ) );
        assertThat( "Contains role Anything-ANY-admin", containRole( roleList, "Anything-ANY-admin" ) );

        assertThat( "Contains privilege permTarget-repo1-cache-create",
            containPrivilegeName( privilegeList, "permTarget-repo1-cache-create" ) );
        assertThat( "Contains privilege permTarget-repo1-cache-read",
            containPrivilegeName( privilegeList, "permTarget-repo1-cache-read" ) );
        assertThat( "Contains privilege permTarget-repo1-cache-update",
            containPrivilegeName( privilegeList, "permTarget-repo1-cache-update" ) );
        assertThat( "Contains privilege permTarget-repo1-cache-delete",
            containPrivilegeName( privilegeList, "permTarget-repo1-cache-delete" ) );

        assertThat( "Contains role permTarget-repo1-cache-reader",
            containRole( roleList, "permTarget-repo1-cache-reader" ) );
        assertThat( "Contains role permTarget-repo1-cache-deployer",
            containRole( roleList, "permTarget-repo1-cache-deployer" ) );
        assertThat( "Contains role permTarget-repo1-cache-delete",
            containRole( roleList, "permTarget-repo1-cache-delete" ) );
        assertThat( "Contains role permTarget-repo1-cache-admin",
            containRole( roleList, "permTarget-repo1-cache-admin" ) );

        // verify user-role mapping
        PlexusUserResource anonymous = getUserById( userList, "anonymous-artifactory" );
        assertThat( anonymous.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( anonymous.getRoles(), "Anything-reader" );

        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        assertThat( admin.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( admin.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        assertThat( user.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( user.getRoles(), "group" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        assertThat( user1.getRoles().size(), is( equalTo( 1 ) ) );
        containPlexusRole( user1.getRoles(), "permTarget1-delete" );

        // verify the group role
        RoleResource groupRole = getRoleById( roleList, "group" );
        assertThat( groupRole, is( notNullValue() ) );
        assertThat( groupRole.getRoles(), hasItem( "permTarget-repo1-cache-reader" ) );
        assertThat( groupRole.getRoles(), hasItem( "permTarget-repo1-cache-deployer" ) );
        assertThat( groupRole.getRoles(), hasItem( "permTarget-repo1-cache-delete" ) );
        assertThat( groupRole.getRoles(), hasItem( "permTarget-repo1-cache-admin" ) );
    }

}
