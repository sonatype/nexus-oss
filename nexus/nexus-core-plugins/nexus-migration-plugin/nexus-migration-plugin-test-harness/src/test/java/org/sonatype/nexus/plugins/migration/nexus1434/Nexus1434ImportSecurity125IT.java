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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;

public class Nexus1434ImportSecurity125IT
    extends AbstractImportSecurityIT
{
    public Nexus1434ImportSecurity125IT()
    {
        super();
    }

    @Override
    public void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-125.zip" ) );

        migrationSummary.setResolvePermission( true );

        commitMigration( migrationSummary );
    }

    @Override
    public void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeStatusResource> privilegeList = getImportedTargetPrivilegesList();
        List<RoleResource> roleList = getImportedRoleList();

        assertThat( "4 users imported", userList.size(), is( equalTo( 4 ) ) );
        assertThat( "3 repo targets imported", targetList.size(), is( equalTo( 3 ) ) );
        assertThat( "4 privileges for each repo target", privilegeList.size(), is( equalTo( targetList.size() * 4 ) ) );
        assertThat( "4 roles for each repo target", roleList.size(), is( equalTo( targetList.size() * 4 ) ) );

        // these users are imported
        assertThat( "User list contains 'admin-artifactory'", containUser( userList, "admin-artifactory" ) );
        assertThat( "User list contains 'admin1'", containUser( userList, "admin1" ) );
        assertThat( "User list contains 'user'", containUser( userList, "user" ) );
        assertThat( "User list contains 'user1'", containUser( userList, "user1" ) );

        for ( RepositoryTargetListResource target : targetList )
        {
            String key = target.getId();

            // 4 privileges for 1 repoTarget imported
            assertThat( "Contains privilege for create",
                containPrivilegeStartAndEndWith( privilegeList, key, "-create" ) );
            assertThat( "Contains privilege for read", containPrivilegeStartAndEndWith( privilegeList, key, "-read" ) );
            assertThat( "Contains privilege for update",
                containPrivilegeStartAndEndWith( privilegeList, key, "-update" ) );
            assertThat( "Contains privilege for delete",
                containPrivilegeStartAndEndWith( privilegeList, key, "-delete" ) );

            // 3 roles for 1 repoTarget imported
            assertThat( "Contains role for reader", containRoleStartAndEndWith( roleList, key, "-reader" ) );
            assertThat( "Contains role for deployer", containRoleStartAndEndWith( roleList, key, "-deployer" ) );
            assertThat( "Contains role for admin", containRoleStartAndEndWith( roleList, key, "-admin" ) );
        }

        // verify user-role mapping
        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        assertThat( admin.getRoles().size(), is(equalTo( 1 )) );
        containRoleEndWith( admin.getRoles(), "admin" );

        PlexusUserResource admin1 = getUserById( userList, "admin1" );
        assertThat( admin1.getRoles().size(), is(equalTo( 1 )) );
        containRoleEndWith( admin1.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        assertThat( user.getRoles().size(), is(equalTo( 3 )) );
        containRoleEndWith( user.getRoles(), "-admin" );
        containRoleEndWith( user.getRoles(), "-deployer" );
        containRoleEndWith( user.getRoles(), "-reader" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        assertThat( user1.getRoles().size(), is(equalTo( 2 )) );
        containRoleEndWith( user1.getRoles(), "-deployer" );
        containRoleEndWith( user1.getRoles(), "-reader" );
    }

}
