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
package org.sonatype.nexus.plugins.migration.nexus1523;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.testng.annotations.Test;

public class Nexus1523ImportToAnExistingRepoIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importToAnExistingRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );

        assertThat( "Migration log file not found", migrationLogFile.isFile() );

        String log = IOUtil.toString( new FileReader( migrationLogFile ) );
        assertThat( "No error during migration \n" + log, !log.toLowerCase().contains( "error" ) );

        File importedArtifact =
            new File( nexusWorkDir, "/storage/main-local/nexus1523/import-artifact/1.0/import-artifact-1.0.jar" );
        assertThat( "Imported artifact do not exists!", importedArtifact.isFile() );
    }

}
