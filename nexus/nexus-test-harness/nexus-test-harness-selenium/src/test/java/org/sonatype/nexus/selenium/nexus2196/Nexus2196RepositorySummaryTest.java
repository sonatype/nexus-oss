package org.sonatype.nexus.selenium.nexus2196;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.internal.matchers.StringContains.containsString;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.NexusTestCase;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositorySummary;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;

public class Nexus2196RepositorySummaryTest
    extends SeleniumTest
{

    @Test
    public void summaryHosted()
        throws InterruptedException
    {
        MockListener ml = listenResult();

        RepositorySummary repo = openSummary( "thirdparty", RepoKind.HOSTED );

        RepositoryMetaResource meta = ( (RepositoryMetaResourceResponse) ml.getResult() ).getData();

        validateRepoInfo( repo, meta );
        validateDistMngt( repo, meta );
    }

    @Test
    public void summaryProxy()
        throws InterruptedException
    {
        MockListener ml = listenResult();

        RepositorySummary repo = openSummary( "central", RepoKind.PROXY );

        RepositoryMetaResource meta = ( (RepositoryMetaResourceResponse) ml.getResult() ).getData();

        validateRepoInfo( repo, meta );
    }

    @Test
    public void summaryShadow()
        throws InterruptedException
    {
        MockListener ml = listenResult();

        RepositorySummary repo = openSummary( "central-m1", RepoKind.VIRTUAL );

        RepositoryMetaResource meta = ( (RepositoryMetaResourceResponse) ml.getResult() ).getData();

        validateRepoInfo( repo, meta );
    }

    @Test
    public void byteSize()
        throws InterruptedException
    {
        RepositorySummary repo = mockSize( 512 );
        Assert.assertThat( repo.getRepositoryInformation().getValue(), containsString( "512 Bytes" ) );
    }

    @Test
    public void kilobyteSize()
    throws InterruptedException
    {
        RepositorySummary repo = mockSize( 524288 );
        Assert.assertThat( repo.getRepositoryInformation().getValue(), containsString( "512 KB" ) );
    }

    @Test
    public void megabyteSize()
    throws InterruptedException
    {
        RepositorySummary repo = mockSize( 536870912 );
        Assert.assertThat( repo.getRepositoryInformation().getValue(), containsString( "512 MB" ) );
    }

    @Test
    public void gigabyteSize()
    throws InterruptedException
    {
        RepositorySummary repo = mockSize( 549755813888L );
        Assert.assertThat( repo.getRepositoryInformation().getValue(), containsString( "512 GB" ) );
    }

    private RepositorySummary mockSize( long size )
    {
        RepositoryMetaResourceResponse result = new RepositoryMetaResourceResponse();
        RepositoryMetaResource data = new RepositoryMetaResource();
        data.setId( "thridparty" );
        data.setFormat( "maven2" );
        data.setRepoType( "hosted" );
        data.setSizeOnDisk( size );
        result.setData( data );

        MockHelper.expect( "/repositories/{repositoryId}/meta", new MockResponse( Status.SUCCESS_OK, result ) );

        RepositorySummary repo = openSummary( "thirdparty", RepoKind.HOSTED );
        return repo;
    }

    private MockListener listenResult()
    {
        MockListener ml = new MockListener()
        {
        };
        MockHelper.listen( "/repositories/{repositoryId}/meta", ml );
        return ml;
    }

    private RepositorySummary openSummary( String repoId, RepoKind kind )
    {
        LoginTest.doLogin( main );
        RepositorySummary repo = main.openRepositories().select( repoId, kind ).selectSummary();
        repo.getRepositoryInformation().waitToLoad();
        return repo;
    }

    private void validateDistMngt( RepositorySummary repo, RepositoryMetaResource meta )
    {
        String distMgmt = repo.getDistributionManagement().getValue();
        Assert.assertThat( distMgmt, notNullValue() );
        Assert.assertThat( distMgmt, containsString( NexusTestCase.nexusBaseURL + "content/repositories/"
            + meta.getId() ) );
    }

    private void validateRepoInfo( RepositorySummary repo, RepositoryMetaResource meta )
    {
        String summary = repo.getRepositoryInformation().getValue();
        Assert.assertThat( summary, notNullValue() );
        Assert.assertThat( summary, containsString( meta.getId() ) );
        Assert.assertThat( summary, containsString( meta.getRepoType() ) );
        Assert.assertThat( summary, containsString( meta.getFormat() ) );
    }
}
