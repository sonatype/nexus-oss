/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.client.rest;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

import junit.framework.Assert;
import junit.framework.TestCase;

public class QuickRestClientTest extends PlexusTestCase
{

    public void testGetList()
        throws NexusConnectionException, NexusClientException
    {
        NexusClient client = new NexusRestClient();
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
    
    public void testIsValidRepository() throws NexusConnectionException, NexusClientException
    {
        
        NexusClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        Assert.assertTrue("Expected to find 'apache-snapshots' repo:", client.isValidRepository( "apache-snapshots" ));
        Assert.assertFalse("Expected not to find 'foobar' repo:", client.isValidRepository( "foobar" ));
        
        Assert.assertFalse("Expected not to find 'null' repo:", client.isValidRepository( null ));
                
        client.disconnect();
        
    }

    public void testGet()
        throws NexusConnectionException, NexusClientException
    {
        NexusClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        RepositoryBaseResource repo = client.getRepository( "releases" );
        Assert.assertEquals( "releases", repo.getId() );
        client.disconnect();
    }

    public void testCrud()
        throws NexusConnectionException, NexusClientException
    {
        NexusClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );

        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "testCreate" );
        repoResoruce.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repoResoruce.setName( "Create Test Repo" );
        // repoResoruce.setRepoType( ? )
        repoResoruce.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        repoResoruce.setAllowWrite( true );
        repoResoruce.setBrowseable( true );
        repoResoruce.setIndexable( true );
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
        Assert.assertFalse( "Expected false, repo should have been deleted.", client.isValidRepository( "testCreate" ) );

        client.disconnect();
    }

    public void testSearchBySHA1() throws NexusClientException, NexusConnectionException
    {
        String sha1 = "72844643827b668a791dfef60cf8c0ea7690d583";
        
        NexusClient client = new NexusRestClient();
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );
        
        NexusArtifact artifact = client.searchBySHA1( sha1 );
        System.out.println( "artifact: "+ artifact.getArtifactId() );
        
        // don't assert anything yet, because this is some junky artfact I uploaded manually...
        
        client.disconnect();
        
    }
    
    public void testSearchByGAV() throws Exception
    {
        
        NexusClient client = (NexusClient) this.lookup( NexusClient.ROLE );;
        client.connect( "http://localhost:8081/nexus", "admin", "admin123" );
        
        NexusArtifact searchParam = new NexusArtifact();
        searchParam.setArtifactId( "release-deploy" );
        searchParam.setGroupId( "org.sonatype.nexus.nexus.test.harness" );
        searchParam.setVersion( "1.0.1" );
        searchParam.setPackaging( "jar" );
        searchParam.setClassifier( "not currently working" );
        
        System.out.println( "value: "+ client.searchByGAV( searchParam ).get( 0 ) );
        
        client.disconnect();
        
    }


}
