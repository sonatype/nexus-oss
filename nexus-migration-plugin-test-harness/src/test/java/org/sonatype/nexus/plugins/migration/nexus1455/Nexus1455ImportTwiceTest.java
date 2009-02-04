package org.sonatype.nexus.plugins.migration.nexus1455;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1455ImportTwiceTest extends AbstractMigrationIntegrationTest
{
    @Test
    public void importTwice()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );
        TaskScheduleUtil.waitForTasks( 40 );
        Thread.sleep( 2000 );
    }


}
