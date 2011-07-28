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
package org.sonatype.nexus.plugins.migration.nexus1434;

import java.util.List;

import junit.framework.Assert;

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
            Assert.assertTrue( containPrivilegeStartAndEndWith( privilegeList, key , "-create" ) );
            Assert.assertTrue( containPrivilegeStartAndEndWith( privilegeList, key , "-read" ) );
            Assert.assertTrue( containPrivilegeStartAndEndWith( privilegeList, key , "-update" ) );
            Assert.assertTrue( containPrivilegeStartAndEndWith( privilegeList, key , "-delete" ) );

            // 3 roles for 1 repoTarget imported
            Assert.assertTrue( containRoleStartAndEndWith( roleList, key , "-reader" ) );
            Assert.assertTrue( containRoleStartAndEndWith( roleList, key , "-deployer" ) );
            Assert.assertTrue( containRoleStartAndEndWith( roleList, key , "-admin" ) );
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
