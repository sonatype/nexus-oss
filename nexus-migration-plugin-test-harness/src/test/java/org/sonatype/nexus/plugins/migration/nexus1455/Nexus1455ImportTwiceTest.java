package org.sonatype.nexus.plugins.migration.nexus1455;

import java.io.File;
import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1455ImportTwiceTest
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importTwice()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );

        File logFile = new File( "./target/logs/migration.log" );
        Assert.assertTrue( "Migration log file not found", logFile.isFile() );

        String log = IOUtil.toString( new FileReader( logFile ) );
        Assert.assertTrue( "Didn't skip second migration", log.contains( "Trying to import the same package twice" ) );
        Assert.assertFalse( "Error during migration", log.toLowerCase().contains( "error" ) );
    }

}
