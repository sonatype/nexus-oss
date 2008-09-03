import java.util.Iterator;
import java.util.List;

import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

import junit.framework.Assert;
import junit.framework.TestCase;

public class QuickRestClientTest
    extends TestCase
{

    public void testGetList()
        throws NexusConnectionException, NexusClientException
    {
        NexusRestClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        List<RepositoryListResource> repos = client.getRespositories();
        Assert.assertTrue( "Expected list of repos to be larger then 0", repos.size() > 0 );
        System.out.println( "list: " + repos );

        for ( Iterator<RepositoryListResource> iter = repos.iterator(); iter.hasNext(); )
        {
            RepositoryListResource repositoryListResource = iter.next();
            System.out.println( "repo: " + repositoryListResource.getId() );
        }
        client.disconnect();
    }

    public void testGet()
        throws NexusConnectionException, NexusClientException
    {
        NexusRestClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        RepositoryBaseResource repo = client.getRepository( "releases" );
        Assert.assertEquals( "releases", repo.getId() );
        client.disconnect();
    }

    public void testCrud()
        throws NexusConnectionException, NexusClientException
    {
        NexusRestClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "testCreate" );
        repoResoruce.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repoResoruce.setName( "Create Test Repo" );
        // repoResoruce.setRepoType( ? )
        repoResoruce.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        // repoResoruce.setAllowWrite( true );
        // repoResoruce.setBrowseable( true );
        // repoResoruce.setIndexable( true );
        // repoResoruce.setNotFoundCacheTTL( 1440 );
        repoResoruce.setRepoPolicy( "release" ); // [snapshot, release] Note: needs param name change
        // repoResoruce.setRealmnId(?)
        // repoResoruce.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // repoResoruce.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // repoResoruce.setDownloadRemoteIndexes( true );
        repoResoruce.setChecksumPolicy( "ignore" ); // [ignore, warn, strictIfExists, strict]

        RepositoryBaseResource repoResult = client.createRepository( repoResoruce );
        RepositoryBaseResource repoExpected = client.getRepository( "testCreate" );

        Assert.assertEquals( repoResult.getId(), repoExpected.getId() );
        Assert.assertEquals( repoResult.getName(), repoExpected.getName() );
        Assert.assertEquals( repoResult.getFormat(), repoExpected.getFormat() );

        // now update it
         repoExpected.setName( "Updated Name" );
         repoExpected = client.updateRepository( repoExpected );
         Assert.assertEquals( "Updated Name", repoExpected.getName() );

        // now delete it
        client.deleteRepository( "testCreate" );

        try
        {
            client.getRepository( "testCreate" );
            Assert.fail( "expected a 404" );
        }
        catch ( NexusConnectionException e )
        {
            // expected
        }

        client.disconnect();
    }


}
