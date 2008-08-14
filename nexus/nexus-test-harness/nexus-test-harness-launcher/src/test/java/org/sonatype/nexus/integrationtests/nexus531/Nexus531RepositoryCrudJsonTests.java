package org.sonatype.nexus.integrationtests.nexus531;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus531RepositoryCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil messageUtil;

    public Nexus531RepositoryCrudJsonTests()
    {
        this.messageUtil =
            new RepositoryMessageUtil( XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),  MediaType.APPLICATION_JSON );
    }
    
    
    
    @Test
    public void createTest()
        throws IOException
    {
        
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "createTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Create Test Repo" );
//        resource.setRepoType( ? )
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
//        resource.setAllowWrite( true );
//        resource.setBrowseable( true );
//        resource.setIndexable( true );
//        resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( "release" ); // [snapshot, release]  Note: needs param name change
//        resource.setRealmnId(?)
//        resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDownloadRemoteIndexes( true );
        resource.setChecksumPolicy( "ignore" ); //[ignore, warn, strictIfExists, strict]

        // this also validates
        this.messageUtil.createRepository( resource );
    }
    
    @Test
    public void readTest() throws IOException
    {
        
        
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "readTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Read Test Repo" );
//        resource.setRepoType( ? )
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
//        resource.setAllowWrite( true );
//        resource.setBrowseable( true );
//        resource.setIndexable( true );
//        resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( "release" ); // [snapshot, release]  Note: needs param name change
//        resource.setRealmnId(?)
//        resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDownloadRemoteIndexes( true );
        resource.setChecksumPolicy( "ignore" ); //[ignore, warn, strictIfExists, strict]

        // this also validates
        this.messageUtil.createRepository( resource ); // this currently also calls GET, but that will change
        
        RepositoryResource responseRepo = this.messageUtil.getRepository( resource.getId() );
        
        // validate they are the same
        this.messageUtil.validateResourceResponse( resource, responseRepo );
        
    }
    
    @Test
    public void updateTest() throws IOException
    {
        
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "updateTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Update Test Repo" );
//        resource.setRepoType( ? )
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
//        resource.setAllowWrite( true );
//        resource.setBrowseable( true );
//        resource.setIndexable( true );
//        resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( "release" ); // [snapshot, release]  Note: needs param name change
//        resource.setRealmnId(?)
//        resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDownloadRemoteIndexes( true );
        resource.setChecksumPolicy( "ignore" ); //[ignore, warn, strictIfExists, strict]

        // this also validates
        resource = this.messageUtil.createRepository( resource );
        
        // udpdate the repo
        resource.setRepoPolicy( "snapshot" );
        
        this.messageUtil.updateRepo( resource );
        
    }
    
    @Test
    public void deleteTest() throws IOException
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "deleteTestRepo" );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Delete Test Repo" );
//        resource.setRepoType( ? )
        resource.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
//        resource.setAllowWrite( true );
//        resource.setBrowseable( true );
//        resource.setIndexable( true );
//        resource.setNotFoundCacheTTL( 1440 );
        resource.setRepoPolicy( "release" ); // [snapshot, release]  Note: needs param name change
//        resource.setRealmnId(?)
//        resource.setOverrideLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDefaultLocalStorageUrl( "" ); //file://repos/internal
//        resource.setDownloadRemoteIndexes( true );
        resource.setChecksumPolicy( "ignore" ); //[ignore, warn, strictIfExists, strict]

        // this also validates
        resource = this.messageUtil.createRepository( resource );
        
        // now delete it...
     // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository: " + response.getStatus() );
        }
        Assert.assertNull( NexusConfigUtil.getRepo( resource.getId() ) ); 
    }
    

}
