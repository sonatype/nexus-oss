/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.migration.nexus1439;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.tasks.RepairIndexTask;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1439ImportMixedRepositoriesIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMixedRepo()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );

        checkRepository( "main-local-releases" );
        checkRepository( "main-local-snapshots" );

        checkGroup( "main-local" );

        checkGroupContent();

        TaskScheduleUtil.waitForAllTasksToStop( RepairIndexTask.class );
        checkIndex( "main-local", "nxcm259", "released", "1.0" );
        checkIndex( "main-local", "nxcm259", "snapshot", "1.0-SNAPSHOT" );

        checkArtifact( "main-local-releases", "nxcm259", "released", "1.0" );
        checkArtifact( "main-local-snapshots", "nxcm259", "snapshot", "1.0-SNAPSHOT" );

        checkNotAvailable( "main-local-releases", "nxcm259", "snapshot", "1.0-SNAPSHOT" );
        checkNotAvailable( "main-local-snapshots", "nxcm259", "released", "1.0" );

        checkArtifactOnGroup( "main-local", "nxcm259", "released", "1.0" );
        checkArtifactOnGroup( "main-local", "nxcm259", "snapshot", "1.0-SNAPSHOT" );
    }

    private void checkGroupContent()
        throws IOException
    {
        RepositoryGroupResource group = this.groupUtil.getGroup( "main-local" );
        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        Assert.assertEquals( 2, repositories.size() );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "main-local-releases" );
        assertContains( reposIds, "main-local-snapshots" );
    }

}
