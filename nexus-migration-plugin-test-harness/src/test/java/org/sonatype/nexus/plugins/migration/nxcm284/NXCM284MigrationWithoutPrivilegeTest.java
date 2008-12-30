package org.sonatype.nexus.plugins.migration.nxcm284;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;

public class NXCM284MigrationWithoutPrivilegeTest
    extends AbstractMigrationPrivilegeTest
{

    @Test
    public void doMigrationWithoutPermission()
        throws Exception
    {
        removePrivilege( TEST_USER_NAME, ARTIFACTORY_MIGRATOR );

        try
        {
            doMigration();
        }
        catch ( Exception e )
        {
            Assert.assertTrue( "Exception message should contain 401 error:\n" + e.getStackTrace(), e
                .getMessage().contains( "401" ) );
        }
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory125.zip" );
    }
}
