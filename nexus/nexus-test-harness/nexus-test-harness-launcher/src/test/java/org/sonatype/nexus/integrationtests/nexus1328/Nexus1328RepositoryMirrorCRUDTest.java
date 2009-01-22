package org.sonatype.nexus.integrationtests.nexus1328;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;

public class Nexus1328RepositoryMirrorCRUDTest
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
    
    private String repositoryId = "release-proxy-repo-1";
    
    public Nexus1328RepositoryMirrorCRUDTest()
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
    public void mirrorStatusTest()
        throws IOException
    {
        MirrorResourceListRequest request = new MirrorResourceListRequest();
        
        MirrorResource resource = new MirrorResource();        
        resource.setUrl( "http://mirrorStatusTest1" );        
        request.addData( resource );
        
        resource = new MirrorResource();        
        resource.setUrl( "http://mirrorStatusTest2" );
        request.addData( resource );

        this.messageUtil.setMirrors( repositoryId, request );
        
        MirrorStatusResourceListResponse response = this.messageUtil.getMirrorsStatus( repositoryId );
        
        MirrorStatusResource one = ( MirrorStatusResource ) response.getData().get( 0 );
        MirrorStatusResource two = ( MirrorStatusResource ) response.getData().get( 1 );
        
        Assert.assertEquals( "http://mirrorStatusTest1", one.getUrl() );
        Assert.assertEquals( "http://mirrorStatusTest2", two.getUrl() );
        Assert.assertEquals( "Available", one.getStatus() );
        Assert.assertEquals( "Available", two.getStatus() );
    }
}
