package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractImportArtifactoryTest
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importArtifactory()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getBackupFile() );
        commitMigration( migrationSummary );

        checkCreation();
        checkLocalRepo();
        checkRemoteRepo();
        checkVirtualRepo();

        TaskScheduleUtil.waitForTasks( 40 );

        Thread.sleep( 3000 );

        checkIndexes();
        checkDownloadArtifacts();
    }

    protected abstract File getBackupFile();

    private void checkDownloadArtifacts()
        throws Exception
    {
        checkArtifact( "nxcm254", "ext-releases", "1.0" );
        checkArtifact( "nxcm254", "ext-snapshots", "1.0-SNAPSHOT" );
        checkArtifact( "nxcm254", "libs-releases", "1.0" );
        checkArtifact( "nxcm254", "libs-snapshots", "1.0-SNAPSHOT" );
        checkArtifact( "nxcm254", "plugins-releases", "1.0" );
        checkArtifact( "nxcm254", "plugins-snapshots", "1.0-SNAPSHOT" );
    }

    protected void checkArtifact( String groupId, String artifactId, String version )
        throws IOException
    {
        super.checkArtifact( artifactId, groupId, artifactId, version );
    }

    private void checkIndexes()
        throws Exception
    {
        checkIndex( "nxcm254", "ext-releases", "1.0" );
        checkIndex( "nxcm254", "ext-snapshots", "1.0-SNAPSHOT" );
        checkIndex( "nxcm254", "libs-releases", "1.0" );
        checkIndex( "nxcm254", "libs-snapshots", "1.0-SNAPSHOT" );
        checkIndex( "nxcm254", "plugins-releases", "1.0" );
        checkIndex( "nxcm254", "plugins-snapshots", "1.0-SNAPSHOT" );
    }

    @SuppressWarnings( "unchecked" )
    private void checkVirtualRepo()
        throws IOException
    {
        RepositoryGroupResource group = this.groupUtil.getGroup( "snapshots-only" );
        Assert.assertNotNull( group );
        Assert.assertEquals( "snapshots-only", group.getId() );

        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        Assert.assertEquals( 4, repositories.size() );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "libs-snapshots" );
        assertContains( reposIds, "plugins-snapshots" );
        assertContains( reposIds, "ext-snapshots" );
        assertContains( reposIds, "codehaus-snapshots" );
    }

    private void checkRemoteRepo()
        throws IOException
    {
        RepositoryProxyResource repo1 = (RepositoryProxyResource) this.repositoryUtil.getRepository( "repo1" );
        Assert.assertNotNull( repo1 );
        Assert.assertEquals( "proxy", repo1.getRepoType() );
        Assert.assertEquals( "release", repo1.getRepoPolicy() );
        Assert.assertEquals( "http://repo1.maven.org/maven2", repo1.getRemoteStorage().getRemoteStorageUrl() );
    }

    private void checkLocalRepo()
        throws IOException
    {
        RepositoryResource libsReleases = (RepositoryResource) this.repositoryUtil.getRepository( "libs-releases" );
        Assert.assertNotNull( libsReleases );
        Assert.assertEquals( "hosted", libsReleases.getRepoType() );
        Assert.assertEquals( "release", libsReleases.getRepoPolicy() );
        Assert.assertEquals( "Local repository for in-house libraries", libsReleases.getName() );
    }

    private void checkCreation()
        throws IOException
    {
        checkRepository( "libs-releases" );
        checkRepository( "libs-snapshots" );
        checkRepository( "plugins-releases" );
        checkRepository( "plugins-snapshots" );
        checkRepository( "ext-releases" );
        checkRepository( "ext-snapshots" );
        checkRepository( "repo1" );
        checkRepository( "codehaus-snapshots" );
        checkRepository( "java.net" );

        checkGroup( "snapshots-only" );
    }

}
