package org.sonatype.nexus.plugins.migration.nexus1523;

import java.io.File;
import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1523ImportToAnExistingRepoIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importToAnExistingRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );

        Assert.assertTrue( "Migration log file not found", migrationLogFile.isFile() );

        String log = IOUtil.toString( new FileReader( migrationLogFile ) );
        Assert.assertFalse( "Error during migration \n" + log, log.toLowerCase().contains( "error" ) );

        File importedArtifact =
            new File( nexusWorkDir, "/storage/main-local/nexus1523/import-artifact/1.0/import-artifact-1.0.jar" );
        Assert.assertTrue( "Imported artifact do not exists!", importedArtifact.isFile() );
    }
}
