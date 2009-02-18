package org.sonatype.nexus.integrationtests.nexus1633;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.repository.metadata.model.io.xpp3.RepositoryMetadataXpp3Reader;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;

public class Nexus1633HostedMirrorMetadataTest
    extends AbstractNexusIntegrationTest
{   
    @BeforeClass
    public static void clean()
    {
        try
        {
            cleanWorkDir();
        }
        catch ( IOException e )
        {
            // NVM
        }
    }
    
    protected MirrorMessageUtils messageUtil;
    
    private String repositoryId = "nexus-test-harness-repo";
    
    public Nexus1633HostedMirrorMetadataTest()
    {
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }
    
    @Test
    public void setMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://setMirrorTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://setMirrorTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );
    }
    
    @Test
    public void updateMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://updateMirrorTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://updateMirrorTest2" );
        request.addData( resource );

        MirrorResourceListResponse response = this.messageUtil.setMirrors( repositoryId, request );
        
        request.setData( response.getData() );
        
        resource = new MirrorResource();
        resource.setUrl( "http://updateMirrorTest3" );
        request.addData( resource );
        
        ( ( MirrorResource ) request.getData().iterator().next() ).setUrl( "http://updateMirrorTest4" );
        
        response = this.messageUtil.setMirrors( repositoryId, request );
        
        MirrorResource one = ( MirrorResource ) response.getData().get( 0 );
        MirrorResource two = ( MirrorResource ) response.getData().get( 1 );
        MirrorResource three = ( MirrorResource ) response.getData().get( 2 );
        
        Assert.assertEquals( "http://updateMirrorTest4", one.getUrl() );
        Assert.assertEquals( "http://updateMirrorTest2", two.getUrl() );
        Assert.assertEquals( "http://updateMirrorTest3", three.getUrl() );
    }
    
    @Test
    public void getMirrorTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://getMirrorTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://getMirrorTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );
        
        MirrorResourceListResponse response = this.messageUtil.getMirrors( repositoryId );
        
        MirrorResource one = ( MirrorResource ) response.getData().get( 0 );
        MirrorResource two = ( MirrorResource ) response.getData().get( 1 );
        
        Assert.assertEquals( "http://getMirrorTest1", one.getUrl() );
        Assert.assertEquals( "http://getMirrorTest2", two.getUrl() );
    }
    
    @Test
    public void validateMetadataChangeOnUpdate()
        throws Exception
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://updateMirrorTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://updateMirrorTest2" );
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://updateMirrorTest3" );
        request.addData( resource );
        
        MirrorResourceListResponse response = messageUtil.setMirrors( repositoryId, request );
        
        Assert.assertEquals( 3, response.getData().size() );
        Assert.assertEquals( "1", ( ( MirrorResource ) response.getData().get( 0 ) ).getId() );
        Assert.assertEquals( "http://updateMirrorTest1", ( ( MirrorResource ) response.getData().get( 0 ) ).getUrl() );
        Assert.assertEquals( "2", ( ( MirrorResource ) response.getData().get( 1 ) ).getId() );
        Assert.assertEquals( "http://updateMirrorTest2", ( ( MirrorResource ) response.getData().get( 1 ) ).getUrl() );
        Assert.assertEquals( "3", ( ( MirrorResource ) response.getData().get( 2 ) ).getId() );
        Assert.assertEquals( "http://updateMirrorTest3", ( ( MirrorResource ) response.getData().get( 2 ) ).getUrl() );
        
        //Now read the metadata
        File metadata = new File( AbstractNexusIntegrationTest.nexusWorkDir + "/storage/" + repositoryId + "/.meta/repository-metadata.xml" );
        
        RepositoryMetadataXpp3Reader reader = new RepositoryMetadataXpp3Reader();
        
        FileInputStream fis = new FileInputStream( metadata );
        
        try
        {
            RepositoryMetadata rm = reader.read( fis );
            
            Assert.assertEquals( 3, rm.getMirrors().size() );
            Assert.assertEquals( "1", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 0 ) ).getId() );
            Assert.assertEquals( "http://updateMirrorTest1", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 0 ) ).getUrl() );
            Assert.assertEquals( "2", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 1 ) ).getId() );
            Assert.assertEquals( "http://updateMirrorTest2", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 1 ) ).getUrl() );
            Assert.assertEquals( "3", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 2 ) ).getId() );
            Assert.assertEquals( "http://updateMirrorTest3", ( ( RepositoryMirrorMetadata ) rm.getMirrors().get( 2 ) ).getUrl() );
        }
        finally
        {
            fis.close();
        }
    }
}
