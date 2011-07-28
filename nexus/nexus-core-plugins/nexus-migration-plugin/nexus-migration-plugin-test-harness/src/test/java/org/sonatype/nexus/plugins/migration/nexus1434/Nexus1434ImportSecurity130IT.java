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

    @SuppressWarnings("unchecked")
    @Override
    protected void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeStatusResource> privilegeList = getImportedTargetPrivilegesList();
        List<RoleResource> roleList = getImportedRoleList();

        Assert.assertEquals( "4 users imported", 4, userList.size() );
        Assert.assertEquals( "3 repo targets imported", 3, targetList.size() );
        Assert.assertEquals( "4 privileges for each repo target", targetList.size() * 4, privilegeList.size() );
        Assert.assertEquals( "4 roles for each repo target, plus a group", targetList.size() * 4 + 1, roleList.size() );

        // these users are imported
        Assert.assertTrue( containUser( userList, "anonymous-artifactory" ) );
        Assert.assertTrue( containUser( userList, "admin-artifactory" ) );
        Assert.assertTrue( containUser( userList, "user" ) );
        Assert.assertTrue( containUser( userList, "user1" ) );


        Assert.assertTrue( containPrivilegeName( privilegeList, "Anything-ANY-create" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "Anything-ANY-read" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "Anything-ANY-update" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "Anything-ANY-delete" ) );

        Assert.assertTrue( containRole( roleList, "Anything-ANY-reader" ) );
        Assert.assertTrue( containRole( roleList, "Anything-ANY-deployer" ) );
        Assert.assertTrue( containRole( roleList, "Anything-ANY-delete" ) );
        Assert.assertTrue( containRole( roleList, "Anything-ANY-admin" ) );

        Assert.assertTrue( containPrivilegeName( privilegeList, "permTarget-repo1-cache-create" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "permTarget-repo1-cache-read" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "permTarget-repo1-cache-update" ) );
        Assert.assertTrue( containPrivilegeName( privilegeList, "permTarget-repo1-cache-delete" ) );

        Assert.assertTrue( containRole( roleList, "permTarget-repo1-cache-reader" ) );
        Assert.assertTrue( containRole( roleList, "permTarget-repo1-cache-deployer" ) );
        Assert.assertTrue( containRole( roleList, "permTarget-repo1-cache-delete" ) );
        Assert.assertTrue( containRole( roleList, "permTarget-repo1-cache-admin" ) );

        // verify user-role mapping
        PlexusUserResource anonymous = getUserById( userList, "anonymous-artifactory" );
        Assert.assertEquals( 1, anonymous.getRoles().size() );
        containPlexusRole( anonymous.getRoles(), "Anything-reader" );

        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        Assert.assertEquals( 1, admin.getRoles().size() );
        containPlexusRole( admin.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        Assert.assertEquals( 1, user.getRoles().size() );
        containPlexusRole( user.getRoles(), "group" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        Assert.assertEquals( 1, user1.getRoles().size() );
        containPlexusRole( user1.getRoles(), "permTarget1-delete" );

        // verify the group role
        RoleResource groupRole = getRoleById(roleList, "group");
        Assert.assertNotNull ( groupRole);
        Assert.assertTrue( groupRole.getRoles().contains( "permTarget-repo1-cache-reader" ) );
        Assert.assertTrue( groupRole.getRoles().contains( "permTarget-repo1-cache-deployer" ) );
        Assert.assertTrue( groupRole.getRoles().contains( "permTarget-repo1-cache-delete" ) );
        Assert.assertTrue( groupRole.getRoles().contains( "permTarget-repo1-cache-admin" ) );
    }

}
