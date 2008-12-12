package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class NXCM254ImportArtifactoryTest
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repositoryUtil;

    private GroupMessageUtil groupUtil;

    public NXCM254ImportArtifactoryTest()
    {
        this.repositoryUtil = new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.groupUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void importArtifactory125()
        throws Exception
    {
        int code = ImportMessageUtil.importBackup( getTestFile( "artifactory125.zip" ) );
        Assert.assertTrue( "Unexpected result from server: " + code, Status.isSuccess( code ) );

        checkCreation();
        checkLocalRepo();
        checkRemoteRepo();
        checkVirtualRepo();
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

    private void assertContains( ArrayList<String> reposIds, String repoId )
    {
        Assert.assertTrue( repoId + " not found.\n" + reposIds, reposIds.contains( repoId ) );
    }

}
