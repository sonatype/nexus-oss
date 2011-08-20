/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.nexus1441;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.MavenDeployer;

public abstract class AbstractDeployBridgeIT
    extends AbstractMigrationIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
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
            getDeployUtils().deployWithWagon( "http", deployUrl, artifact, path );
        }

        File deployedFile = new File( nexusWorkDir, "/storage/" + targetRepository + "/" + path );
        if ( numberOfFiles == -1 )
        {
            assertThat( "Artifact was not deployed", deployedFile.exists() );
            assertThat( "Deployed artifact was not right, checksum comparation fail " + deployUrl,
                FileTestingUtils.compareFileSHA1s( artifact, deployedFile ) );
        }
        else
        {
            assertThat( "Artifact was not deployed ", deployedFile.getParentFile().list().length,
                is( equalTo( numberOfFiles ) ) );
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
