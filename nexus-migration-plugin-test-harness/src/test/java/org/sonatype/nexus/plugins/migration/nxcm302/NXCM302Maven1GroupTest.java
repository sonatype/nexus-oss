package org.sonatype.nexus.plugins.migration.nxcm302;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public class NXCM302Maven1GroupTest
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
