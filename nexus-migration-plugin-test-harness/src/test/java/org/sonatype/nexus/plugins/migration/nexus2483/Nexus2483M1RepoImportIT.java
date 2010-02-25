package org.sonatype.nexus.plugins.migration.nexus2483;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus2483M1RepoImportIT
extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMixedRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090810.161252.zip" ) );
        commitMigration( migrationSummary );

    }

}
