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
package org.sonatype.nexus.plugins.migration.nexus1445;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.testng.annotations.Test;

public class Nexus1445Maven1GroupIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void testMaven1Group()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );

        RepositoryResolutionDTO repo = migrationSummary.getRepositoryResolution( "repo1" );
        assertThat( "Central repository not found", repo, is( notNullValue() ) );
        assertThat( "Central repository not marked to merge", repo.getSimilarRepositoryId(), is( notNullValue() ) );
        repo.setMergeSimilarRepository( true );

        GroupResolutionDTO group = migrationSummary.getGroupResolution( "remote-repos" );
        assertThat( "Group not found", group, is( notNullValue() ) );
        assertThat( "Group should contains maven 1 and 2 repositories", group.isMixed() );
        group.setRepositoryTypeResolution( ERepositoryTypeResolution.VIRTUAL_BOTH );

        commitMigration( migrationSummary );

        RepositoryBaseResource virtualRepo = repositoryUtil.getRepository( "java.net.m1-releases-virtual" );
        assertThat( "Virtual release repository was not created", virtualRepo, is( notNullValue() ) );
        virtualRepo = repositoryUtil.getRepository( "java.net.m1-snapshots-virtual" );
        assertThat( "Virtual snapshot repository was not created", virtualRepo, is( notNullValue() ) );
    }

}
