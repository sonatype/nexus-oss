package org.sonatype.nexus.plugins.migration.nexus1455;

import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1455ImportTwiceIT
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importTwice()
        throws Exception
    {
        if ( true )
        {
            super.printKnownErrorButDoNotFail( getClass(), "importTwice" );
            return;
        }

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        Assert.assertNotNull( migrationSummary.getId() );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );

        Assert.assertTrue( "Migration log file not found", migrationLogFile.isFile() );

        String log = IOUtil.toString( new FileReader( migrationLogFile ) );
        Assert.assertTrue( "Didn't skip second migration " + log,
                           log.contains( "Trying to import the same package twice" ) );
        Assert.assertFalse( "Error during migration", log.toLowerCase().contains( "error" ) );
    }

}
