package org.sonatype.nexus.plugins.migration.nxcm254;

import hidden.org.codehaus.plexus.util.StringUtils;

import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;

public class NXCM254ImportSecurity130RepoKeyValidateTest
    extends AbstractImportSecurityTest
{
    public NXCM254ImportSecurity130RepoKeyValidateTest()
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
    protected void verifySecurity()
        throws Exception
    {
        List<PrivilegeBaseStatusResource> privilegeList = getImportedPrivilegeList();

        for ( PrivilegeBaseStatusResource priv : privilegeList )
        {
            PrivilegeTargetStatusResource targetPriv = (PrivilegeTargetStatusResource) priv;

            String repoId = targetPriv.getRepositoryId();

            String groupId = targetPriv.getRepositoryGroupId();

            if ( !StringUtils.isEmpty( repoId ) )
            {
                assertRepoIdExists( repoId );
            }
            if ( !StringUtils.isEmpty( groupId ) )
            {
                assertRepoGroupIdExists( groupId );
            }
        }

    }

    private void assertRepoIdExists( String id )
        throws Exception
    {
        Assert.assertNotNull( repoUtil.getRepository( id ) );
    }

    private void assertRepoGroupIdExists( String id )
        throws Exception
    {
        Assert.assertNotNull( groupUtil.getGroup( id ) );
    }

}
