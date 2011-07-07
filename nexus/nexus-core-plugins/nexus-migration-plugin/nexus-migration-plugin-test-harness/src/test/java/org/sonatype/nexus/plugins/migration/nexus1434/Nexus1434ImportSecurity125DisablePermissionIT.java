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
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;

public class Nexus1434ImportSecurity125DisablePermissionIT
    extends AbstractImportSecurityIT
{

    public Nexus1434ImportSecurity125DisablePermissionIT()
    {
        super();
    }

    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-125.zip" ) );

        // this is the most important part!
        migrationSummary.setResolvePermission( false );

        commitMigration( migrationSummary );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeStatusResource> privilegeList = getImportedTargetPrivilegesList();
        List<RoleResource> roleList = getImportedRoleList();

        Assert.assertEquals( 4, userList.size() );
        Assert.assertTrue( targetList.isEmpty() );
        Assert.assertTrue( privilegeList.isEmpty() );
        Assert.assertTrue( roleList.isEmpty() );

        // these users are imported
        Assert.assertTrue( containUser( userList, "admin-artifactory" ) );
        Assert.assertTrue( containUser( userList, "admin1" ) );
        Assert.assertTrue( containUser( userList, "user" ) );
        Assert.assertTrue( containUser( userList, "user1" ) );

        // verify user-role mapping
        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        Assert.assertEquals( 1, admin.getRoles().size() );
        containPlexusRole( admin.getRoles(), "admin" );

        PlexusUserResource admin1 = getUserById( userList, "admin1" );
        Assert.assertEquals( 1, admin1.getRoles().size() );
        containPlexusRole( admin1.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        Assert.assertEquals( 1, user.getRoles().size() );
        containPlexusRole( user.getRoles(), "anonymous" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        Assert.assertEquals( 1, user1.getRoles().size() );
        containPlexusRole( user1.getRoles(), "anonymous" );
    }

}
