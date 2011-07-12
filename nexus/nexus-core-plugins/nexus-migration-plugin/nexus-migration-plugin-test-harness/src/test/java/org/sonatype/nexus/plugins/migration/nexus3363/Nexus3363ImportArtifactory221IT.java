package org.sonatype.nexus.plugins.migration.nexus3363;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.plugins.migration.nexus1434.AbstractImportSecurityIT;

public class Nexus3363ImportArtifactory221IT
    extends AbstractImportSecurityIT
{

    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-221.zip" ) );

        List<UserResolutionDTO> userList = migrationSummary.getUsersResolution();

        List<UserResolutionDTO> returnUserList = new ArrayList<UserResolutionDTO>();

        returnUserList.add( userList.get( 0 ) );

        returnUserList.add( userList.get( 1 ) );

        migrationSummary.setUsersResolution( returnUserList );

        migrationSummary.setResolvePermission( true );

        commitMigration( migrationSummary );
    }

    @Override
    protected void verifySecurity()
        throws Exception
    {
        Assert.assertEquals( 2, getImportedUserList().size() );
    }

}
