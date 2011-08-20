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
package org.sonatype.nexus.plugins.migration.nexus1449;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.testng.annotations.Test;

public class Nexus1449ImportMaven2OnlyIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMaven2()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );

        GroupResolutionDTO group = migrationSummary.getGroupResolution( "remote-repos" );
        assertThat( "Group not found", group, is( notNullValue() ) );
        assertThat( "Group should contains maven 1 and 2 repositories", group.isMixed() );
        group.setRepositoryTypeResolution( ERepositoryTypeResolution.MAVEN_2_ONLY );

        commitMigration( migrationSummary );

        // just be sure if repos are there
        RepositoryBaseResource javaRepo = repositoryUtil.getRepository( "java.net.m2" );
        assertThat( "Virtual release repository was not created", javaRepo, is( notNullValue() ) );
        javaRepo = repositoryUtil.getRepository( "java.net.m1" );
        assertThat( "Virtual release repository was not created", javaRepo, is( notNullValue() ) );

        RepositoryGroupResource remoteGroup = groupUtil.getGroup( "remote-repos" );
        assertThat( "Only one repo should be included", remoteGroup.getRepositories().size(), is( equalTo( 1 ) ) );

        RepositoryGroupMemberRepository m2Repo = remoteGroup.getRepositories().get( 0 );
        assertThat( "m2 releases is not imported", m2Repo.getId(), is( equalTo( "java.net.m2" ) ) );
    }

}
