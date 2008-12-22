package org.sonatype.nexus.plugins.migration.nxcm254;

import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RoleResource;

public class NXCM254ImportSecurity125WithoutPermissionTest
    extends AbstractImportSecurityTest
{

    public NXCM254ImportSecurity125WithoutPermissionTest()
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
        List<PrivilegeBaseStatusResource> privilegeList = getImportedPrivilegeList();
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
        containRoleEndWith( admin.getRoles(), "admin" );

        PlexusUserResource admin1 = getUserById( userList, "admin1" );
        Assert.assertEquals( 1, admin1.getRoles().size() );
        containRoleEndWith( admin1.getRoles(), "admin" );

        PlexusUserResource user = getUserById( userList, "user" );
        Assert.assertEquals( 1, user.getRoles().size() );
        containRoleEndWith( user.getRoles(), "anonymous" );

        PlexusUserResource user1 = getUserById( userList, "user1" );
        Assert.assertEquals( 1, user1.getRoles().size() );
        containRoleEndWith( user1.getRoles(), "anonymous" );
    }

}
