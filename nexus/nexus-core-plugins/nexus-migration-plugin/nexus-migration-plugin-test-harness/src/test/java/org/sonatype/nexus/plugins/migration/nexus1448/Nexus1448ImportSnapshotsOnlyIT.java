package org.sonatype.nexus.plugins.migration.nexus1448;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.EMixResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1448ImportSnapshotsOnlyIT
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importSnapshotsOnly()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        RepositoryResolutionDTO mainLocal = migrationSummary.getRepositoriesResolution().get( 0 );
        mainLocal.setMixResolution( EMixResolution.SNAPSHOTS_ONLY );
        commitMigration( migrationSummary );

        checkArtifact( "main-local", "nexus1448", "snapshot", "1.0-SNAPSHOT" );
        checkArtifactNotPresent( "main-local", "nexus1448", "released", "1.0" );
    }
}
