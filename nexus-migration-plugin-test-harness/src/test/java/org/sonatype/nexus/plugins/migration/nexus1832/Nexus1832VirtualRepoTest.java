package org.sonatype.nexus.plugins.migration.nexus1832;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1832VirtualRepoTest
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMixedRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "backup.zip" ) );
        commitMigration( migrationSummary );

        checkRepository( "plugins-releases" );
        checkRepository( "plugins-snapshots" );
        checkRepository( "libs-releases" );
        checkRepository( "libs-snapshots" );
        checkRepository( "ext-releases" );
        checkRepository( "ext-snapshots" );

        checkGroup( "repo" );

        checkIndex( "repo", "nexus1832", "plugins-release", "1.0" );
        checkIndex( "repo", "nexus1832", "ext-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "ext-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "ext-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "plugins-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "plugins-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "libs-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "libs-snapshots", "1.0-SNAPSHOT" );
    }

}
