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
package org.sonatype.nexus.plugins.migration.nexus1448;

import org.sonatype.nexus.plugin.migration.artifactory.dto.EMixResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.testng.annotations.Test;

public class Nexus1448ImportReleasesOnlyIT
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importReleasesOnly()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        RepositoryResolutionDTO mainLocal = migrationSummary.getRepositoriesResolution().get( 0 );
        mainLocal.setMixResolution( EMixResolution.RELEASES_ONLY );
        commitMigration( migrationSummary );

        checkArtifact( "main-local", "nexus1448", "released", "1.0" );
        checkArtifactNotPresent( "main-local", "nexus1448", "snapshot", "1.0-SNAPSHOT" );
    }
}
