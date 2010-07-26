package org.sonatype.nexus.integrationtests.nexus3615;


import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;

public class Nexus3615ArtifactInfoProviderIT
    extends AbstractArtifactInfoIT
{

    @Test
    public void getInfo()
        throws Exception
    {
        ArtifactInfoResource info =
            getSearchMessageUtil().getInfo( REPO_TEST_HARNESS_REPO, "nexus3615/artifact/1.0/artifact-1.0.jar" );

        Assert.assertEquals( REPO_TEST_HARNESS_REPO, info.getRepositoryId() );
        Assert.assertEquals( "/nexus3615/artifact/1.0/artifact-1.0.jar", info.getRepositoryPath() );
        Assert.assertEquals( "b354a0022914a48daf90b5b203f90077f6852c68", info.getSha1Hash() );
        Assert.assertEquals( 3, info.getRepositories().size() );
        Assert.assertThat( getRepositoryId( info.getRepositories() ), IsCollectionContaining.hasItems(
            REPO_TEST_HARNESS_REPO, REPO_TEST_HARNESS_REPO2, REPO_TEST_HARNESS_RELEASE_REPO ) );
        Assert.assertEquals( "application/java-archive", info.getMimeType() );
        Assert.assertEquals( 1364, info.getSize() );
    }
}
