package org.sonatype.nexus.plugins.migration;

import java.io.File;

import org.junit.BeforeClass;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;

public abstract class AbstractMigrationPrivilegeTest
    extends AbstractPrivilegeTest
{

    @BeforeClass
    public static void enableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    protected final String ARTIFACTORY_MIGRATOR = "artifactory-migrator";

    abstract protected File getBackupFile();

    protected Status doMigration()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = ImportMessageUtil.importBackup( getBackupFile() );

        ImportMessageUtil.fillDefaultEmailIfNotExist( migrationSummary.getUserResolution() );

        return ImportMessageUtil.commitImport( migrationSummary ).getStatus();
    }

}
