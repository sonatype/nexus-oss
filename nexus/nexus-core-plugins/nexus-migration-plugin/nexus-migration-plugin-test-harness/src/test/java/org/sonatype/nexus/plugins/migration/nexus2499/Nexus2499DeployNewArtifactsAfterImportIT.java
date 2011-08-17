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
package org.sonatype.nexus.plugins.migration.nexus2499;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus2499DeployNewArtifactsAfterImportIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void deployAfterImport()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090818.120005.zip" ) );
        commitMigration( migrationSummary );

        checkDeployment( "ext-releases-local", false );
        checkDeployment( "ext-snapshots-local", true );
        checkDeployment( "libs-releases-local", false );
        checkDeployment( "libs-snapshots-local", true );
        checkDeployment( "plugins-releases-local", false );
        checkDeployment( "plugins-snapshots-local", true );
    }

    private void checkDeployment( String repoId, boolean snapshot )
        throws Exception
    {
        checkRepository( repoId );
        File artifactFile = getTestFile( "artifact.jar" );
        Gav g =
            new Gav( "org.sonatype.test", repoId, snapshot ? "1.0-SNAPSHOT" : "1.0", null, "jar", null, null, null,
                     snapshot, false, null, false, null );
        String path = this.getRelitiveArtifactPath( g );
        getDeployUtils().deployWithWagon( "http", nexusBaseUrl + "content/repositories/" + repoId, artifactFile, path );

        File dArtifact = downloadArtifactFromRepository( repoId, g, "target/download/nexus2499" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifactFile, dArtifact ) );

        RepositoryResource repo = (RepositoryResource) repositoryUtil.getRepository( repoId );
        Assert.assertEquals( RepositoryWritePolicy.ALLOW_WRITE.name(), repo.getWritePolicy() );
    }
}
