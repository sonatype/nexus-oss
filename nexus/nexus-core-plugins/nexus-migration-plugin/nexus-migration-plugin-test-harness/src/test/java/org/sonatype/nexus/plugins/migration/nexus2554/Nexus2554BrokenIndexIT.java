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
package org.sonatype.nexus.plugins.migration.nexus2554;

import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.testng.annotations.Test;

public class Nexus2554BrokenIndexIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void brokenIndex()
        throws Exception
    {
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

        String logs = FileUtils.fileRead( getNexusLogFile() );
        assertThat(
            logs,
            !logs.contains( "Error message is: java.lang.NullPointerException Strack trace: java.lang.NullPointerException" ) );
        assertThat( logs, !logs.contains( "RepositoryNotAvailableException" ) );
        assertThat( logs, !logs.contains( "java.lang.NullPointerException" ) );
    }

}
