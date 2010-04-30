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
package org.sonatype.nexus.integrationtests.nexus531;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

/**
 * CRUD tests for JSON request/response.
 */
public class Nexus531RepositoryCrudJsonIT
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil messageUtil;

    public Nexus531RepositoryCrudJsonIT()
        throws ComponentLookupException
    {
        this.messageUtil =
            new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );
    }

    @Test
    public void createRepositoryTest()
        throws IOException
    {

        RepositoryResource resource = new RepositoryResource();

        resource.setId( "createTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Create Test Repo" );
        // resource.setRepoType( ? )
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        // resource.setAllowWrite( true );
        // resource.setBrowseable( true );
        // resource.setIndexable( true );
        // resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // resource.setRealmnId(?)
        // resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDownloadRemoteIndexes( true );
        // only valid for proxy repos resource.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

        // this also validates
        this.messageUtil.createRepository( resource );
    }

    @Test
    public void readTest()
        throws IOException
    {

        RepositoryResource resource = new RepositoryResource();

        resource.setId( "readTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Read Test Repo" );
        // resource.setRepoType( ? )
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        // resource.setAllowWrite( true );
        // resource.setBrowseable( true );
        // resource.setIndexable( true );
        // resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // resource.setRealmnId(?)
        // resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDownloadRemoteIndexes( true );
        // only valid for proxy repos resource.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

        // this also validates
        this.messageUtil.createRepository( resource ); // this currently also calls GET, but that will change

        RepositoryResource responseRepo = (RepositoryResource) this.messageUtil.getRepository( resource.getId() );

        // validate they are the same
        this.messageUtil.validateResourceResponse( resource, responseRepo );

    }

    @Test
    public void updateTest()
        throws IOException
    {

        RepositoryResource resource = new RepositoryResource();

        resource.setId( "updateTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Update Test Repo" );
        // resource.setRepoType( ? )
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        // resource.setAllowWrite( true );
        // resource.setBrowseable( true );
        // resource.setIndexable( true );
        // resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // resource.setRealmnId(?)
        // resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDownloadRemoteIndexes( true );
        // only valid for proxy repos resource.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

        // this also validates
        resource = (RepositoryResource) this.messageUtil.createRepository( resource );

        // udpdate the repo
        resource.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );

        this.messageUtil.updateRepo( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "deleteTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Delete Test Repo" );
        // resource.setRepoType( ? )
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        // resource.setAllowWrite( true );
        // resource.setBrowseable( true );
        // resource.setIndexable( true );
        // resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // resource.setRealmnId(?)
        // resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
        // resource.setDownloadRemoteIndexes( true );
        // only valid for proxy repos resource.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

        // this also validates
        resource = (RepositoryResource) this.messageUtil.createRepository( resource );

        // now delete it...
        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository: " + response.getStatus() );
        }
        Assert.assertNull( NexusConfigUtil.getRepo( resource.getId() ) );
    }

    @Test
    public void listTest()
        throws IOException
    {

        RepositoryResource repo = new RepositoryResource();

        repo.setId( "listTestRepo" );
        repo.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repo.setName( "List Test Repo" );
        repo.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() ); // [snapshot, release] Note: needs param name change
        // only valid for proxy repos repo.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]

        // this also validates
        repo = (RepositoryResource) this.messageUtil.createRepository( repo );

        // now get the lists
        List<RepositoryListResource> repos = this.messageUtil.getList();

        for ( Iterator<RepositoryListResource> iter = repos.iterator(); iter.hasNext(); )
        {
            RepositoryListResource listRepo = iter.next();

            if ( listRepo.getId().equals( repo.getId() ) )
            {
                Assert.assertEquals( repo.getId(), listRepo.getId() );
                Assert.assertEquals( repo.getName(), listRepo.getName() );
                Assert.assertEquals( repo.getFormat(), listRepo.getFormat() );
                Assert.assertEquals( repo.getRepoPolicy(), listRepo.getRepoPolicy() );
                Assert.assertEquals( repo.getRepoType(), listRepo.getRepoType() );
                Assert.assertEquals( repo.getRemoteStorage(), listRepo.getRemoteUri() );

                String storageURL =
                    repo.getDefaultLocalStorageUrl() != null ? repo.getDefaultLocalStorageUrl()
                                    : repo.getOverrideLocalStorageUrl();
                    
                storageURL = storageURL.endsWith( "/" ) ? storageURL : storageURL + "/";
                String effectiveLocalStorage = listRepo.getEffectiveLocalStorageUrl().endsWith( "/" ) ? listRepo.getEffectiveLocalStorageUrl() : listRepo.getEffectiveLocalStorageUrl() + "/";
                    
                Assert.assertEquals( storageURL, effectiveLocalStorage );
            }

            // now check all agaist the the cRepo
            CRepository cRepo = NexusConfigUtil.getRepo( listRepo.getId() );

            if ( cRepo != null )
            {
                M2RepositoryConfiguration cM2Repo = NexusConfigUtil.getM2Repo( listRepo.getId() );
                Assert.assertEquals( cRepo.getId(), listRepo.getId() );
                Assert.assertEquals( cRepo.getName(), listRepo.getName() );
                // Assert.assertEquals( cM2Repo.getType(), listRepo.getFormat() );
                Assert.assertEquals( cM2Repo.getRepositoryPolicy().name(), listRepo.getRepoPolicy() );

                log.debug( "cRepo.getRemoteStorage(): " + cRepo.getRemoteStorage() );
                log.debug( "listRepo.getRemoteUri(): " + listRepo.getRemoteUri() );

                Assert.assertTrue( ( cRepo.getRemoteStorage() == null && listRepo.getRemoteUri() == null )
                    || ( cRepo.getRemoteStorage().getUrl().equals( listRepo.getRemoteUri() ) ) );
            }
            else
            {
                M2LayoutedM1ShadowRepositoryConfiguration cShadow = NexusConfigUtil.getRepoShadow( listRepo.getId() );

                Assert.assertEquals( cRepo.getId(), listRepo.getId() );
                Assert.assertEquals( cRepo.getName(), listRepo.getName() );
                // Assert.assertEquals( cShadow.getType(), this.formatToType( listRepo.getFormat() ) );
                Assert.assertEquals( AbstractRepositoryPlexusResource.REPO_TYPE_VIRTUAL, listRepo.getRepoType() );
            }

        }
    }

    // private String formatToType( String format )
    // {
    // Map<String, String> formatToTypeMap = new HashMap<String, String>();
    // formatToTypeMap.put( "maven2", "m1-m2-shadow" );
    // formatToTypeMap.put( "maven1", "m2-m1-shadow" );
    //
    // return formatToTypeMap.get( format );
    // }

}
