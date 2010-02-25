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
package org.sonatype.nexus.plugins.migration.nexus1449;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;

public class Nexus1449ImportMaven2OnlyIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importMaven2()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );

        GroupResolutionDTO group = migrationSummary.getGroupResolution( "remote-repos" );
        Assert.assertNotNull( "Group not found", group );
        Assert.assertTrue( "Group should contains maven 1 and 2 repositories", group.isMixed() );
        group.setRepositoryTypeResolution( ERepositoryTypeResolution.MAVEN_2_ONLY );

        commitMigration( migrationSummary );

        // just be sure if repos are there
        RepositoryBaseResource javaRepo = repositoryUtil.getRepository( "java.net.m2" );
        Assert.assertNotNull( "Virtual release repository was not created", javaRepo );
        javaRepo = repositoryUtil.getRepository( "java.net.m1" );
        Assert.assertNotNull( "Virtual release repository was not created", javaRepo );

        RepositoryGroupResource remoteGroup = groupUtil.getGroup( "remote-repos" );
        Assert.assertEquals( "Only one repo should be included", 1, remoteGroup.getRepositories().size() );

        RepositoryGroupMemberRepository m2Repo =
            remoteGroup.getRepositories().get( 0 );
        Assert.assertEquals( "m2 releases is not imported", "java.net.m2", m2Repo.getId() );

    }

}
