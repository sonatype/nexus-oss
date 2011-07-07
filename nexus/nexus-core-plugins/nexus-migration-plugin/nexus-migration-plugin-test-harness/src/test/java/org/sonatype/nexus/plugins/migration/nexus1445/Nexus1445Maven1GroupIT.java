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
package org.sonatype.nexus.plugins.migration.nexus1445;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public class Nexus1445Maven1GroupIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void testMaven1Group()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );

        RepositoryResolutionDTO repo = migrationSummary.getRepositoryResolution( "repo1" );
        Assert.assertNotNull( "Central repository not found", repo );
        Assert.assertNotNull( "Central repository not marked to merge", repo.getSimilarRepositoryId() );
        repo.setMergeSimilarRepository( true );

        GroupResolutionDTO group = migrationSummary.getGroupResolution( "remote-repos" );
        Assert.assertNotNull( "Group not found", group );
        Assert.assertTrue( "Group should contains maven 1 and 2 repositories", group.isMixed() );
        group.setRepositoryTypeResolution( ERepositoryTypeResolution.VIRTUAL_BOTH );

        commitMigration( migrationSummary );

        RepositoryBaseResource virtualRepo = repositoryUtil.getRepository( "java.net.m1-releases-virtual" );
        Assert.assertNotNull( "Virtual release repository was not created", virtualRepo );
        virtualRepo = repositoryUtil.getRepository( "java.net.m1-snapshots-virtual" );
        Assert.assertNotNull( "Virtual snapshot repository was not created", virtualRepo );
    }

}
