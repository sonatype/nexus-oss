package org.sonatype.nexus.plugins.migration.nxcm259;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class NXCM259ImportMixedRepositoriesTest
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

        TaskScheduleUtil.waitForTasks( 40 );
        Thread.sleep( 2000 );

        checkIndex( "nxcm259", "released", "1.0" );
        checkIndex( "nxcm259", "snapshot", "1.0-SNAPSHOT" );

        checkArtifact( "main-local-releases", "nxcm259", "released", "1.0" );
        checkArtifact( "main-local-snapshots", "nxcm259", "snapshot", "1.0-SNAPSHOT" );

        checkNotAvailable( "main-local-releases", "nxcm259", "snapshot", "1.0-SNAPSHOT" );
        checkNotAvailable( "main-local-snapshots", "nxcm259", "released", "1.0" );

        checkArtifactOnGroup( "main-local", "nxcm259", "released", "1.0" );
        checkArtifactOnGroup( "main-local", "nxcm259", "snapshot", "1.0-SNAPSHOT" );
    }

    @SuppressWarnings( "unchecked" )
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

    protected void checkNotAvailable( String repositoryId, String groupId, String artifactId, String version )
        throws IOException
    {
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        try
        {
            downloadArtifactFromRepository( repositoryId, gav, "target/downloads/nxcm259" );
            Assert.fail( "Artifact available at wrong repository " + artifactId );
        }
        catch ( FileNotFoundException e )
        {
            // expected
        }

    }

}
