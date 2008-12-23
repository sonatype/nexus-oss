package org.sonatype.nexus.plugins.migration.nxcm280;

import java.io.File;

import org.junit.Assert;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class AbstractDeployBridgeTest
    extends AbstractMigrationIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );

        TaskScheduleUtil.waitForTasks( 40 );
        Thread.sleep( 2000 );
    }

    protected void deploy( Gav gav, String repositoryId, boolean useMavenDeployer, int numberOfFiles,
                           String targetRepository )
        throws Exception
    {
        File artifact = getTestFile( "artifact.jar" );
        String path = getRelitiveArtifactPath( gav );
        String deployUrl = "http://localhost:" + nexusApplicationPort + "/artifactory/" + repositoryId;
        if ( useMavenDeployer )
        {
            MavenDeployer.deployAndGetVerifier( gav, deployUrl, artifact, getOverridableFile( "settings.xml" ) );
        }
        else
        {
           DeployUtils. deployWithWagon( this.container, "http", deployUrl, artifact, path );
        }

        File deployedFile = new File( nexusWorkDir, "/storage/" + targetRepository + "/" + path );
        if ( numberOfFiles == -1 )
        {
            Assert.assertTrue( "Artifact was not deployed", deployedFile.exists() );
            Assert.assertTrue( "Deployed artifact was not right, checksum comparation fail " + deployUrl,
                               FileTestingUtils.compareFileSHA1s( artifact, deployedFile ) );
        }
        else
        {
            Assert.assertEquals( "Artifact was not deployed ", numberOfFiles,
                                 deployedFile.getParentFile().list().length );
        }
    }

    protected void deploy( Gav gav, String repositoryId, boolean useMavenDeployer, int numberOfFiles )
        throws Exception
    {
        deploy( gav, repositoryId, useMavenDeployer, numberOfFiles, repositoryId );
    }

    protected void deploy( Gav gav, String repositoryId, boolean useMavenDeployer )
        throws Exception
    {
        deploy( gav, repositoryId, useMavenDeployer, -1 );
    }

    protected void deploy( Gav gav, String repositoryId, boolean useMavenDeployer, String targetRepository )
        throws Exception
    {
        deploy( gav, repositoryId, useMavenDeployer, -1, targetRepository );
    }

}
