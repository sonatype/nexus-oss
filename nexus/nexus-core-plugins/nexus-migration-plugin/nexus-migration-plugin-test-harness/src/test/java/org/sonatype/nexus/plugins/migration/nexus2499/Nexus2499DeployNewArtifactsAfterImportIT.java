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
