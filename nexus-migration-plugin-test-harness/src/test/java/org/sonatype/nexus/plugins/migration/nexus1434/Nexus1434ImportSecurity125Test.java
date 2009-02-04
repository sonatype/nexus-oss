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
package org.sonatype.nexus.plugins.migration.nexus1434;

import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PrivilegeStatusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RoleResource;

public class Nexus1434ImportSecurity125Test
    extends AbstractImportSecurityTest
{
    public Nexus1434ImportSecurity125Test()
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

    @SuppressWarnings( "unchecked" )
    @Override
    public void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeStatusResource> privilegeList = getImportedPrivilegeList();
        List<RoleResource> roleList = getImportedRoleList();

        Assert.assertEquals( "4 users imported", 4, userList.size() );
        Assert.assertEquals( "3 repo targets imported", 3, targetList.size() );
        Assert.assertEquals( "4 privileges for each repo target", targetList.size() * 4, privilegeList.size() );
        Assert.assertEquals( "4 roles for each repo target", targetList.size() * 4, roleList.size() );

        // these users are imported
        Assert.assertTrue( containUser( userList, "admin-artifactory" ) );
        Assert.assertTrue( containUser( userList, "admin1" ) );
        Assert.assertTrue( containUser( userList, "user" ) );
        Assert.assertTrue( containUser( userList, "user1" ) );

        for ( RepositoryTargetListResource target : targetList )
        {
            String key = target.getId();

            // 4 privileges for 1 repoTarget imported
            Assert.assertTrue( containPrivilegeName( privilegeList, key + "-create" ) );
            Assert.assertTrue( containPrivilegeName( privilegeList, key + "-read" ) );
            Assert.assertTrue( containPrivilegeName( privilegeList, key + "-update" ) );
            Assert.assertTrue( containPrivilegeName( privilegeList, key + "-delete" ) );

            // 3 roles for 1 repoTarget imported
            Assert.assertTrue( containRole( roleList, key + "-reader" ) );
            Assert.assertTrue( containRole( roleList, key + "-deployer" ) );
            Assert.assertTrue( containRole( roleList, key + "-admin" ) );
        }

        // verify user-role mapping
        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        Assert.assertEquals( 1, admin.getRoles().size() );
        containRoleEndWith( admin.getRoles(), "admin" );

        PlexusUserResource admin1 = getUserById( userList, "admin1" );
        Assert.assertEquals( 1, admin1.getRoles().size() );
        containRoleEndWith( admin1.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        Assert.assertEquals( 3, user.getRoles().size() );
        containRoleEndWith( user.getRoles(), "-admin" );
        containRoleEndWith( user.getRoles(), "-deployer" );
        containRoleEndWith( user.getRoles(), "-reader" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        Assert.assertEquals( 2, user1.getRoles().size() );
        containRoleEndWith( user1.getRoles(), "-deployer" );
        containRoleEndWith( user1.getRoles(), "-reader" );
    }


}
