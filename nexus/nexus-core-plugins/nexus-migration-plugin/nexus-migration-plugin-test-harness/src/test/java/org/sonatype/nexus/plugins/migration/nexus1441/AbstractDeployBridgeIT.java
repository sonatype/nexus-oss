/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.migration.nexus1441;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
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
