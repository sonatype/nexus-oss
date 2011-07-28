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

public class Nexus1449ImportMaven1OnlyIT
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
        group.setRepositoryTypeResolution( ERepositoryTypeResolution.MAVEN_1_ONLY );

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
        Assert.assertEquals( "m2 releases is not imported", "java.net.m1", m2Repo.getId() );

    }

}
