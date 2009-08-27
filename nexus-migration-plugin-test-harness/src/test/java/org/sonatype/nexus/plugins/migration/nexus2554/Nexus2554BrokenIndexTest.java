package org.sonatype.nexus.plugins.migration.nexus2554;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus2554BrokenIndexTest
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void brokenIndex()
        throws Exception
    {
        File nexusLog = new File( "./target/logs/nexus.log" );
        if ( nexusLog.isFile() )
        {
            FileUtils.fileWrite( nexusLog.getAbsolutePath(), "" );
        }

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090825.081708.zip" ) );
        commitMigration( migrationSummary );

        checkRepository( "ext-snapshots-local" );
        checkRepository( "libs-releases-local" );
        checkRepository( "libs-snapshots-local" );
        checkRepository( "plugins-releases-local" );
        checkRepository( "plugins-snapshots-local" );
        checkRepository( "ext-releases-local" );
        checkRepository( "repo1" );
        checkSnapshotReleaseRepository( "java.net.m2" );
        checkSnapshotReleaseRepository( "java.net.m1" );
        checkRepository( "jboss" );
        checkRepository( "codehaus" );
        checkSnapshotReleaseRepository( "sss" );
        checkSnapshotReleaseRepository( "abc" );
        checkSnapshotReleaseRepository( "xyz" );

        checkGroup( "remote-repos" );
        checkGroup( "libs-releases" );
        checkGroup( "plugins-releases" );
        checkGroup( "libs-snapshots" );
        checkGroup( "plugins-snapshots" );
        checkGroup( "vvv" );

        String logs = FileUtils.fileRead( nexusLog );
        Assert.assertFalse(
                            logs,
                            logs.contains( "Error message is: java.lang.NullPointerException Strack trace: java.lang.NullPointerException" ) );
        Assert.assertFalse( logs, logs.contains( "RepositoryNotAvailableException" ) );
    }

}
