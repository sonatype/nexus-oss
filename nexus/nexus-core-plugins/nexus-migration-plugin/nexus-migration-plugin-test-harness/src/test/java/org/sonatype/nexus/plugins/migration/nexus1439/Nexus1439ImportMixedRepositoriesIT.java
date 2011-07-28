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
