package org.sonatype.nexus.plugins.migration.nxcm254;

import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RoleResource;

public class NXCM254ImportSecurity130DisablePermissionTest
    extends AbstractImportSecurityTest
{

    public NXCM254ImportSecurity130DisablePermissionTest()
    {
        super();
    }

    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-130.zip" ) );

        migrationSummary.setResolvePermission( false );

        commitMigration( migrationSummary );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void verifySecurity()
        throws Exception
    {
        List<PlexusUserResource> userList = getImportedUserList();
        List<RepositoryTargetListResource> targetList = getImportedRepoTargetList();
        List<PrivilegeBaseStatusResource> privilegeList = getImportedPrivilegeList();
        List<RoleResource> roleList = getImportedRoleList();

        Assert.assertEquals( 4, userList.size() );
        Assert.assertTrue( targetList.isEmpty() );
        Assert.assertTrue( privilegeList.isEmpty() );
        // the group is coverted to a role now
        Assert.assertEquals( 1, roleList.size() );

        // these users are imported
        Assert.assertTrue( containUser( userList, "anonymous-artifactory" ) );
        Assert.assertTrue( containUser( userList, "admin-artifactory" ) );
        Assert.assertTrue( containUser( userList, "user" ) );
        Assert.assertTrue( containUser( userList, "user1" ) );

        // verify user-role mapping
        PlexusUserResource anonymous = getUserById( userList, "anonymous-artifactory" );
        Assert.assertEquals( 1, anonymous.getRoles().size() );
        containPlexusRole( anonymous.getRoles(), "anonymous" );

        PlexusUserResource admin = getUserById( userList, "admin-artifactory" );
        Assert.assertEquals( 1, admin.getRoles().size() );
        containPlexusRole( admin.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        Assert.assertEquals( 1, user.getRoles().size() );
        containPlexusRole( user.getRoles(), "group" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        Assert.assertEquals( 1, user1.getRoles().size() );
        containPlexusRole( user1.getRoles(), "anonymous" );

        // verify the group role
        RoleResource groupRole = getRoleById( roleList, "group" );
        Assert.assertNotNull( groupRole );
        Assert.assertTrue( groupRole.getRoles().contains( "anonymous" ) );
    }

}
