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
package org.sonatype.nexus.plugins.migration.nexus1832;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.tasks.RepairIndexTask;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1832VirtualRepoIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMixedRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "backup.zip" ) );
        commitMigration( migrationSummary );

        checkRepository( "plugins-releases" );
        checkRepository( "plugins-snapshots" );
        checkRepository( "libs-releases" );
        checkRepository( "libs-snapshots" );
        checkRepository( "ext-releases" );
        checkRepository( "ext-snapshots" );

        checkGroup( "repo" );

        TaskScheduleUtil.waitForAllTasksToStop( RepairIndexTask.class );
        checkIndex( "plugins-releases", "nexus1832", "plugins-releases", "1.0" );
        checkIndex( "repo", "nexus1832", "plugins-releases", "1.0" );
        checkIndex( "ext-snapshots", "nexus1832", "ext-snapshots", "1.0-SNAPSHOT" );
        checkIndex( "repo", "nexus1832", "ext-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "ext-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "ext-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "plugins-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "plugins-snapshots", "1.0-SNAPSHOT" );

        checkArtifactOnGroup( "repo", "nexus1832", "libs-releases", "1.0" );
        checkArtifactOnGroup( "repo", "nexus1832", "libs-snapshots", "1.0-SNAPSHOT" );
    }

}
