package org.sonatype.nexus.plugins.migration.nexus3343;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;

public abstract class AbstractBigFileIT
    extends AbstractMigrationIntegrationTest
{

    protected static final Gav GAV;
    static
    {
        try
        {
            GAV = GavUtil.newGav( "nexus3343", "released", "1.0", "bin" );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void test()
        throws Exception
    {
        FileTestingUtils.populate( getSourceFile(), 5 );
        File result = doTest();

        Assert.assertTrue( "File don't match", FileTestingUtils.compareFileSHA1s( result, getSourceFile() ) );
    }

    public abstract File getSourceFile();

    public abstract File doTest()
        throws Exception;

}
