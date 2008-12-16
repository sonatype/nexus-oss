package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractImportArtifactoryTest
    extends AbstractMigrationIntegrationTest
{

    private RepositoryMessageUtil repositoryUtil;

    private GroupMessageUtil groupUtil;

    private SearchMessageUtil searchUtil;

    public AbstractImportArtifactoryTest()
    {
        this.repositoryUtil = new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.groupUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.searchUtil = new SearchMessageUtil();
    }

    @Test
    public void importArtifactory()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = ImportMessageUtil.importBackup( getBackupFile() );
        Assert.assertNotNull( "Unexpected result from server: " + migrationSummary, migrationSummary );

        Status status = ImportMessageUtil.commitImport( migrationSummary ).getStatus();
        Assert.assertTrue( "Unable to commit import " + status, status.isSuccess() );

        checkCreation();
        checkLocalRepo();
        checkRemoteRepo();
        checkVirtualRepo();

        TaskScheduleUtil.waitForTasks( 40 );

        Thread.sleep( 2000 );

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

    private void checkArtifact( String groupId, String artifactId, String version )
        throws IOException
    {
        File artifact = getTestFile( "artifact.jar" );
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        File downloaded;
        try
        {
            downloaded = downloadArtifactFromRepository( artifactId, gav, "target/downloads/nxcm254" );
        }
        catch ( IOException e )
        {
            Assert.fail( "Unable to download artifact " + artifactId + " got:\n" + e.getMessage() );
            throw e; // never happen
        }

        Assert.assertTrue( "Downloaded artifact was not right, checksum comparation fail " + artifactId,
                           FileTestingUtils.compareFileSHA1s( artifact, downloaded ) );
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

    private void checkIndex( String groupId, String artifactId, String version )
        throws Exception
    {
        List<NexusArtifact> artifacts = searchUtil.searchFor( groupId, artifactId, version );
        Assert.assertEquals( "Expected to found only one artifact (" + artifactId + ") instead of " + artifacts.size()
            + "\n" + artifacts, 1, artifacts.size() );
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
        ArrayList<String> reposIds = new ArrayList<String>();
        List<RepositoryListResource> repositories = this.repositoryUtil.getList();
        for ( RepositoryListResource repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "libs-releases" );
        assertContains( reposIds, "libs-snapshots" );
        assertContains( reposIds, "plugins-releases" );
        assertContains( reposIds, "plugins-snapshots" );
        assertContains( reposIds, "ext-releases" );
        assertContains( reposIds, "ext-snapshots" );
        assertContains( reposIds, "repo1" );
        assertContains( reposIds, "codehaus-snapshots" );
        assertContains( reposIds, "java.net" );

        ArrayList<String> groupsIds = new ArrayList<String>();
        List<RepositoryGroupListResource> groups = this.groupUtil.getList();
        for ( RepositoryGroupListResource group : groups )
        {
            groupsIds.add( group.getId() );
        }
        assertContains( groupsIds, "snapshots-only" );
    }

}
