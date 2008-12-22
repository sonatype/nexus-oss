package org.sonatype.nexus.plugins.migration.nxcm254;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;

public class NXCM254ImportSecurity125RemoveUserTest
    extends AbstractImportSecurityTest
{

    public NXCM254ImportSecurity125RemoveUserTest()
    {
        super();
    }
    
    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-125.zip" ) );

        List<UserResolutionDTO> userList = migrationSummary.getUserResolution();

        List<UserResolutionDTO> returnUserList = new ArrayList<UserResolutionDTO>();

        returnUserList.add( userList.get( 0 ) );

        returnUserList.add( userList.get( 1 ) );

        migrationSummary.setUserResolution( returnUserList );

        commitMigration( migrationSummary );
    }

    @Override
    protected void verifySecurity()
        throws Exception
    {
        Assert.assertEquals( 2, getImportedUserList().size() );
    }

}
