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
