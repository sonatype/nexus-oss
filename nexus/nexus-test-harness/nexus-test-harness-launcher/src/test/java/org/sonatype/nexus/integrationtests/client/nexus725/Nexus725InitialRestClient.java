/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.client.nexus725;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

/**
 * Tests the Nexus java/REST client.
 */
public class Nexus725InitialRestClient
    extends AbstractPrivilegeTest
{

    private NexusClient getConnectedNexusClient()
        throws Exception
    {

        NexusClient client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.baseNexusUrl, context.getAdminUsername(),
                        context.getAdminPassword() );

        return client;
    }

    @Test
    public void getRepoListTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        List<RepositoryListResource> repos = client.getRespositories();
        Assert.assertTrue( "Expected list of repos to be larger then 0", repos.size() > 0 );

        List<String> knownRepos = new ArrayList<String>();
        knownRepos.add( "fake-central" );
        knownRepos.add( "nexus-test-harness-repo" );
        knownRepos.add( "nexus-test-harness-repo2" );
        knownRepos.add( "nexus-test-harness-release-repo" );
        knownRepos.add( "nexus-test-harness-snapshot-repo" );
        knownRepos.add( "release-proxy-repo-1" );
        knownRepos.add( "nexus-test-harness-shadow" );

        for ( Iterator<RepositoryListResource> iter = repos.iterator(); iter.hasNext(); )
        {
            RepositoryListResource repositoryListResource = iter.next();
            Assert.assertTrue( "Expected to find repo: " + repositoryListResource.getId() + " in list: " + knownRepos,
                               knownRepos.contains( repositoryListResource.getId() ) );
        }
        client.disconnect();
    }

    @Test
    public void isValidRepositoryTest()
        throws Exception
    {

        NexusClient client = this.getConnectedNexusClient();

        Assert.assertTrue( "Expected to find 'apache-snapshots' repo:",
                           client.isValidRepository( "nexus-test-harness-repo" ) );
        Assert.assertFalse( "Expected not to find 'foobar' repo:", client.isValidRepository( "foobar" ) );

        Assert.assertFalse( "Expected not to find 'null' repo:", client.isValidRepository( null ) );

        client.disconnect();

    }

    @Test
    public void getRepoTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        RepositoryBaseResource repo = client.getRepository( "nexus-test-harness-repo" );
        Assert.assertEquals( "nexus-test-harness-repo", repo.getId() );
        client.disconnect();
    }

    @Test
    public void repoCrudTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "testCreate" );
        repoResoruce.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repoResoruce.setName( "Create Test Repo" );
        // repoResoruce.setRepoType( ? )
        repoResoruce.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        repoResoruce.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        repoResoruce.setAllowWrite( true );
        repoResoruce.setBrowseable( true );
        repoResoruce.setIndexable( true );
        // repoResoruce.setNotFoundCacheTTL( 1440 );
        repoResoruce.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // repoResoruce.setRealmnId(?)
        // repoResoruce.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // repoResoruce.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // repoResoruce.setDownloadRemoteIndexes( true );
        repoResoruce.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

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

    @Test
    public void searchBySHA1Test()
        throws Exception
    {
        // will fail do to problems with the indexer
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "searchBySHA1Test" ) )
        {
            return;
        }

        String sha1 = "72844643827b668a791dfef60cf8c0ea7690d583";

        NexusClient client = this.getConnectedNexusClient();

        NexusArtifact artifact = client.searchBySHA1( sha1 );
        System.out.println( "artifact: " + artifact );

        // don't assert anything yet, because this is some junky artfact I uploaded manually...

        client.disconnect();

    }

    @Test
    public void searchByGAVTest()
        throws Exception
    {

        NexusClient client = this.getConnectedNexusClient();

        NexusArtifact searchParam = new NexusArtifact();
        searchParam.setArtifactId( "nexus725-artifact-1" );
        searchParam.setGroupId( "nexus725" );
        searchParam.setVersion( "1.0.1" );
        searchParam.setPackaging( "jar" );
        searchParam.setClassifier( null );

        List<NexusArtifact> results = client.searchByGAV( searchParam );
        Assert.assertEquals( "Search result size", 1, results.size() );

        Assert.assertEquals( "Search result artifact id", "nexus725-artifact-1", results.get( 0 ).getArtifactId() );
        Assert.assertEquals( "Search result group id", "nexus725", results.get( 0 ).getGroupId() );
        Assert.assertEquals( "Search result version", "1.0.1", results.get( 0 ).getVersion() );
        Assert.assertEquals( "Search result packaging", "jar", results.get( 0 ).getPackaging() );

        client.disconnect();

    }

    @Test
    public void checkForErrorsInRepsonse()
        throws Exception
    {

        NexusClient client = this.getConnectedNexusClient();

        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "checkForErrorsInRepsonse" );
        repoResoruce.setName( "Create Test Repo" );
        repoResoruce.setRepoType( "hosted" ); // TODO: REMOVE this
        // this will cause a few problems...
        try
        {
            client.createRepository( repoResoruce );
            Assert.fail( "Expected NexusConnectionException" );
        }
        catch ( NexusConnectionException e )
        {
            // make sure we have an error
            Assert.assertTrue( "NexusConnectionException should contain at least 1 NexusError",
                               e.getErrors().size() > 0 );

            // make sure the error is in the stacktrace
            Assert.assertTrue( "Expected message in error", e.getMessage().contains( e.getErrors().get( 0 ).getMsg() ) );
        }
    }

    @Test
    public void invalidServer()
        throws Exception
    {

        NexusClient client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        try
        {
            client.connect( "http://nexus.invalid.url/nexus", "", "" );
            // the REST instance doesn't actually connect until you send a message
            client.getRepository( "nexus-test-harness-repo" );
            Assert.fail( "Expected NexusConnectionException" );
        }
        catch ( NexusConnectionException e )
        {
            // expected
        }

    }

    @Test
    public void invalidPassword()
        throws Exception
    {
        NexusClient client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );

        try
        {
            client.connect( AbstractNexusIntegrationTest.baseNexusUrl, "admin", "wrong-password" );
            // the REST instance doesn't actually connect until you send a message
            client.getRepository( "nexus-test-harness-repo" );
            Assert.fail( "Expected NexusConnectionException" );
        }
        catch ( NexusConnectionException e )
        {
        }
    }

}
