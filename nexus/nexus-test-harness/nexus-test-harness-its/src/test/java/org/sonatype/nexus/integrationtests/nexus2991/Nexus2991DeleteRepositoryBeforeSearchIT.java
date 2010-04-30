package org.sonatype.nexus.integrationtests.nexus2991;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

/**
 * @author juven
 */
public class Nexus2991DeleteRepositoryBeforeSearchIT
    extends AbstractPrivilegeTest
{
    private NexusClient getConnectedNexusClient()
        throws Exception
    {
        NexusClient client = (NexusClient) lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.nexusBaseUrl, context.getAdminUsername(), context
            .getAdminPassword() );

        return client;
    }

    @Test
    public void searchBeforeAndAfterDeletingRepository()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();
        NexusArtifact searchParam = new NexusArtifact();
        searchParam.setArtifactId( "nexus2991-artifact" );
        searchParam.setGroupId( "nexus2991" );
        searchParam.setVersion( "1.0.1" );
        searchParam.setPackaging( "jar" );
        searchParam.setClassifier( null );
        List<NexusArtifact> results = client.searchByGAV( searchParam );
        Assert.assertEquals( "Search result size", 1, results.size() );
        client.disconnect();

        // create a repo
        client = this.getConnectedNexusClient();
        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "testCreate" );
        repoResoruce.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repoResoruce.setName( "Create Test Repo" );
        repoResoruce.setProvider( "maven2" );
        repoResoruce.setProviderRole( Repository.class.getName() );
        repoResoruce.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        repoResoruce.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repoResoruce.setBrowseable( true );
        repoResoruce.setIndexable( true );
        repoResoruce.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repoResoruce.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]
        RepositoryBaseResource repoResult = client.createRepository( repoResoruce );
        Assert.assertNotNull( repoResult );
        RepositoryBaseResource repoExpected = client.getRepository( "testCreate" );
        Assert.assertEquals( repoResult.getId(), repoExpected.getId() );
        Assert.assertEquals( repoResult.getName(), repoExpected.getName() );
        Assert.assertEquals( repoResult.getFormat(), repoExpected.getFormat() );
        // delete it
        client.deleteRepository( "testCreate" );
        client.disconnect();

        // give some time to nexus for update index
        Thread.sleep( 1000 );

        // search again
        client = this.getConnectedNexusClient();
        results = client.searchByGAV( searchParam );
        Assert.assertEquals( "Search result size", 1, results.size() );
        client.disconnect();
    }
}
