package org.sonatype.nexus.plugins.migration.nxcm284;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;

public class NXCM284MigrationWithPrivilegeTest
    extends AbstractMigrationPrivilegeTest
{
    @Test
    public void doMigrationWithPermission()
        throws Exception
    {
        addPrivilege( TEST_USER_NAME, ARTIFACTORY_MIGRATOR );

        Status status = doMigration();

        Assert.assertTrue( status.isSuccess() );
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory125.zip" );
    }
}
