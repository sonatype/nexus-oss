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
